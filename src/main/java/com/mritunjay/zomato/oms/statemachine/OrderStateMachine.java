package com.mritunjay.zomato.oms.statemachine;

import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.exception.InvalidStateTransitionException;

import java.util.Map;
import java.util.Set;

public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> transitions = Map.of(

            // Order just created → moves to payment pending or can be cancelled
            OrderStatus.CREATED, Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED),

            // Payment step → either success (CONFIRMED) or cancellation
            OrderStatus.PAYMENT_PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),

            // Order confirmed → restaurant starts preparing or user cancels
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),

            // Food is being prepared → next step is out for delivery
            OrderStatus.PREPARING, Set.of(OrderStatus.OUT_FOR_DELIVERY),

            // Delivery in progress → final state is delivered
            OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED),

            // Terminal states → no further transitions allowed
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()

    );

    public static boolean cannotTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowedStates = transitions.getOrDefault(from, Set.of());
        return !allowedStates.contains(to);
    }

}
















