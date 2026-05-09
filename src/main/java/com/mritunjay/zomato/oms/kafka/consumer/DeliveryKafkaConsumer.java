package com.mritunjay.zomato.oms.kafka.consumer;

import com.mritunjay.zomato.oms.event.PaymentSuccessEvent;
import com.mritunjay.zomato.oms.service.DeliveryService;
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
public class DeliveryKafkaConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "${kafka.topic.payment-success}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentSuccess(
                @Payload PaymentSuccessEvent event,
                @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                @Header(KafkaHeaders.OFFSET) long offset,
                Acknowledgment acknowledgment
    ) {

        log.info("Received PaymentSuccessEvent from partition [{}] offset [{}] for orderId [{}]", partition, offset, event.getOrderId());

        try {
            deliveryService.assignPartner(event.getOrderId());

            acknowledgment.acknowledge();
            log.info("Delivery assigned and offset acknowledged for orderId [{}]", event.getOrderId());

        } catch (Exception ex) {

            log.error("Error assigning delivery for orderId [{}]: {}", event.getOrderId(), ex.getMessage(), ex);
            acknowledgment.acknowledge();

        }

    }

}
