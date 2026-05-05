package com.mritunjay.zomato.oms.service;

import com.mritunjay.zomato.oms.delivery.assignment.AssignmentStrategy;
import com.mritunjay.zomato.oms.dto.Delivery.DeliveryResponseDTO;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.exception.InvalidStateTransitionException;
import com.mritunjay.zomato.oms.exception.NoDeliveryPartnerAvailableException;
import com.mritunjay.zomato.oms.exception.OrderNotFoundException;
import com.mritunjay.zomato.oms.mapper.Delivery.DeliveryMapper;
import com.mritunjay.zomato.oms.model.DeliveryPartner;
import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.repository.DeliveryPartnerRepository;
import com.mritunjay.zomato.oms.repository.OrderRepository;
import com.mritunjay.zomato.oms.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final AssignmentStrategy strategy;
    private final DeliveryMapper deliveryMapper;

    @Retryable(
            retryFor = {NoDeliveryPartnerAvailableException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public DeliveryResponseDTO assignPartner(Long orderId) {

        log.info("Attempting to assign delivery partner for order {}", orderId);
        return doAssignPartner(orderId);

    }

    @Transactional
    public DeliveryResponseDTO doAssignPartner(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Idempotency: skip re-assignment if already assigned
        if(order.getDeliveryPartner() != null) {
            log.info("Order {} already has a delivery partner, skipping reassignment", orderId);
            return deliveryMapper.convertOrderEntityToDeliveryResponseDto(order);
        }

        // FIX: State should be CONFIRMED → PREPARING before OUT_FOR_DELIVERY.
        // DeliveryService assigns partner and moves order to OUT_FOR_DELIVERY.
        // Valid transition here is CONFIRMED → OUT_FOR_DELIVERY only if PREPARING
        // was already done. In our flow, payment confirms the order, then delivery
        // is triggered — so we validate CONFIRMED → OUT_FOR_DELIVERY is now
        // allowed only after PREPARING. Since PaymentSuccessEvent fires right after
        // CONFIRMED, we keep CONFIRMED → PREPARING → OUT_FOR_DELIVERY sequential.
        // Here we assume order is in CONFIRMED state (from payment), and we
        // first move it to PREPARING, then to OUT_FOR_DELIVERY.
        if(OrderStateMachine.cannotTransition(order.getStatus(), OrderStatus.PREPARING)) {
            throw new InvalidStateTransitionException(order.getStatus(), OrderStatus.PREPARING);
        }

        // Lock and fetch available partners
        List<DeliveryPartner> partners = partnerRepository.findAvailableForUpdate();
        if(partners.isEmpty()) {
            // Use  exception so @Retryable knows what to retry on
            throw new NoDeliveryPartnerAvailableException();
        }

        log.info("Found {} available delivery partners", partners.size());

        // Assign using strategy (random / nearest etc.)
        DeliveryPartner assigned = strategy.assign(partners);
        log.info("Assigned delivery partner {} to order {}", assigned.getId(), orderId);

        // mark partner unavailable and save
        assigned.setAvailable(false);
        partnerRepository.save(assigned);

        // Move order through PREPARING -> OUT_FOR_DELIVERY
        order.setStatus(OrderStatus.PREPARING);
        order.setDeliveryPartner(assigned);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} is now OUT FOR DELIVERY", orderId);

        return deliveryMapper.convertOrderEntityToDeliveryResponseDto(savedOrder);

    }

    // Called after all retries are exhausted
    @Recover
    public DeliveryResponseDTO recover(NoDeliveryPartnerAvailableException ex, Long orderId) {
        log.error("All retries exhausted. Could not assign delivery partner for order {}", orderId);
        throw new RuntimeException("Delivery assignment failed after retries for order: " + orderId);
    }

}
