package com.mritunjay.zomato.oms.service;

import com.mritunjay.zomato.oms.delivery.assignment.AssignmentStrategy;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.model.DeliveryPartner;
import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.repository.DeliveryPartnerRepository;
import com.mritunjay.zomato.oms.repository.OrderRepository;
import com.mritunjay.zomato.oms.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final AssignmentStrategy strategy;

    @Transactional
    public Order assignPartner(Long orderId) {

        // Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Prevent re-assignment
        if(order.getDeliveryPartner() != null) {
            throw new RuntimeException("Order already assigned");
        }

        // Validate state transition
        if(!OrderStateMachine.canTransition(order.getStatus(), OrderStatus.OUT_FOR_DELIVERY)) {
            throw new RuntimeException("Invalid state for assignment");
        }

        // Lock and fetch available partners
        List<DeliveryPartner> partners = partnerRepository.findAvailableForUpdate();
        if(partners.isEmpty()) {
            throw new RuntimeException("No delivery partners available");
        }

        // Assign partner
        DeliveryPartner assigned = strategy.assign(partners);

        // Mark partner unavailable
        assigned.setAvailable(false);

        // Link oder to partner
        order.setDeliveryPartner(assigned);

        // Update order status
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        return orderRepository.save(order);

    }

}
