package com.mritunjay.zomato.oms.strategy.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentStrategy implements PaymentStrategy{

    @Override
    public boolean process(Double amount) {

        log.info("Processing Card payment of ₹{}", amount);
        // In real world: call Stripe/Razorpay card API here
        return true;

    }

    @Override
    public String getMethod() {
        return "CARD";
    }

}
