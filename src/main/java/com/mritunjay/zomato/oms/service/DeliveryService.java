package com.mritunjay.zomato.oms.service;

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
import com.mritunjay.zomato.oms.strategy.delivery.AssignmentStrategy;
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

        log.info("Attempting delivery assignment for orderId [{}]", orderId);
        return doAssignPartner(orderId);

    }

    @Transactional
    public DeliveryResponseDTO doAssignPartner(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Idempotency: skip re-assignment if already assigned
        if(order.getDeliveryPartner() != null) {
            log.info("Order [{}] already has partner [{}], skipping", orderId, order.getDeliveryPartner().getId());
            return deliveryMapper.convertOrderEntityToDeliveryResponseDto(order);
        }

        // State machine validation
        // Order must be CONFIRMED before we can move it to PREPARING
        if(OrderStateMachine.cannotTransition(order.getStatus(), OrderStatus.PREPARING)) {
            throw new InvalidStateTransitionException(order.getStatus(), OrderStatus.PREPARING);
        }

        // Pessimistic lock: fetch available partners
        // SELECT ... FOR UPDATE ensures no two transactions assign the same partner
        List<DeliveryPartner> partners = partnerRepository.findAvailableForUpdate();
        if(partners.isEmpty()) {
            throw new NoDeliveryPartnerAvailableException(); // triggers retry
        }

        log.info("Found {} available partners for orderId [{}]", partners.size(), orderId);

        // Strategy: pick partner (Random or Nearest)
        // Currently picks Nearest Delivery partner as it is marked with @Primary
        DeliveryPartner assigned = strategy.assign(partners);
        log.info("Assigned partner [{}] to orderId [{}]", assigned.getId(), orderId);

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
        log.error("All 3 retry attempts exhausted for orderId [{}]. No partner available.", orderId);
        throw new RuntimeException("Delivery assignment failed after retries for order: " + orderId);
    }

}
