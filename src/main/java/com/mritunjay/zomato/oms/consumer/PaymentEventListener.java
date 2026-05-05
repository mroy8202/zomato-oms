package com.mritunjay.zomato.oms.consumer;

import com.mritunjay.zomato.oms.dto.Payment.PaymentRequestDTO;
import com.mritunjay.zomato.oms.event.OrderCreatedEvent;
import com.mritunjay.zomato.oms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentService paymentService;

    @EventListener
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("Received OrderCreatedEvent for order {}", event.getOrderId());

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId(event.getOrderId());
        request.setAmount(event.getTotalAmount());
        request.setPaymentMethod("UPI");
        request.setIdempotencyKey("auto-" + event.getOrderId());

        paymentService.processPayment(request);

    }

}
