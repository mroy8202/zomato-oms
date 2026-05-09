package com.mritunjay.zomato.oms.strategy.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpiPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean process(Double amount) {

        log.info("Processing UPI payment of ₹{}", amount);
        // In real world: call GooglePay/PhonePe API here
        return true;

    }

    @Override
    public String getMethod() {
        return "UPI";
    }

}
