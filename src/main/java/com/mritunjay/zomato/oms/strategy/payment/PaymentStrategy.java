package com.mritunjay.zomato.oms.strategy.payment;

public interface PaymentStrategy {

    boolean process(Double amount);
    String getMethod();

}
