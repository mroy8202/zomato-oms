package com.mritunjay.zomato.oms.kafka.consumer;

import com.mritunjay.zomato.oms.dto.Payment.PaymentRequestDTO;
import com.mritunjay.zomato.oms.event.OrderCreatedEvent;
import com.mritunjay.zomato.oms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${kafka.topic.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(
                @Payload OrderCreatedEvent event,
                @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                @Header(KafkaHeaders.OFFSET) long offset,
                Acknowledgment acknowledgment
    ) {

        log.info("Received OrderCreatedEvent from partition [{}] offset [{}] for orderId [{}]", partition, offset, event.getOrderId());

        try {

            PaymentRequestDTO request = new PaymentRequestDTO();
            request.setOrderId(event.getOrderId());
            request.setAmount(event.getTotalAmount());
            request.setPaymentMethod("UPI");
            request.setIdempotencyKey("auto-" + event.getOrderId());

            paymentService.processPayment(request);

            // SUCCESS: commit offset — tell Kafka we're done with this message
            acknowledgment.acknowledge();
            log.info("Payment processed and offset acknowledged for orderId [{}]", event.getOrderId());

        } catch (Exception ex) {

            log.error("Error processing payment for orderId [{}]: {}", event.getOrderId(), ex.getMessage(), ex);
            acknowledgment.acknowledge();

        }
    }

}
