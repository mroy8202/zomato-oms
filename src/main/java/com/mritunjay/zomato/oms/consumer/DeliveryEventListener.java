package com.mritunjay.zomato.oms.consumer;

import com.mritunjay.zomato.oms.event.PaymentSuccessEvent;
import com.mritunjay.zomato.oms.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventListener {

    private final DeliveryService deliveryService;

    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received PaymentSuccessEvent, assigning delivery for order {}", event.getOrderId());
        deliveryService.assignPartner(event.getOrderId());
    }

}
