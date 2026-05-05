package com.mritunjay.zomato.oms.exception;

import com.mritunjay.zomato.oms.enums.OrderStatus;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(OrderStatus from, OrderStatus to) {
        super("Cannot transition order status from " + from + " to " + to);
    }

}
