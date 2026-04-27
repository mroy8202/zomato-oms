package com.mritunjay.zomato.oms.statemachine;

import com.mritunjay.zomato.oms.enums.OrderStatus;

import java.util.Map;
import java.util.Set;

public class OrderStateMachine {

    // current state -> allowed next states
    private static final Map<OrderStatus, Set<OrderStatus>> transitions = Map.of(

            // Order just created → can go to payment or be cancelled
            OrderStatus.CREATED, Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED),

            // Payment step → either success (CONFIRMED) or cancellation
            OrderStatus.PAYMENT_PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),

            // Order confirmed → restaurant starts preparing OR user cancels
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),

            // Food is being prepared → next step is delivery
            OrderStatus.PREPARING,Set.of(OrderStatus.OUT_FOR_DELIVERY),

            // Delivery in progress → final state is delivered
            OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED),

            // Terminal states → no further transitions allowed
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()

    );

    public static boolean canTransition(OrderStatus from, OrderStatus to) {

        // Get allowed next states for the current state
        Set<OrderStatus> allowedStates = transitions.getOrDefault(from, Set.of());

        // Check if desired state is in allowed transitions
        return allowedStates.contains(to);

    }

}
