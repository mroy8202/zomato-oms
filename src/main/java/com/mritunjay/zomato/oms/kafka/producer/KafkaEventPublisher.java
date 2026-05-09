package com.mritunjay.zomato.oms.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topic.payment-success}")
    private String paymentSuccessTopic;

    public void publishOrderCreated(Object event, Long orderId) {

        publish(orderCreatedTopic, orderId, event);

    }

    public void publishPaymentSuccess(Object event, Long orderId) {

        publish(paymentSuccessTopic, orderId, event);

    }

    private void publish(String topic, Long orderId, Object event) {

        // Key = orderId ensures all events for an order go to the same partition (ordered)
        String key = orderId.toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic [{}] for orderId [{}]: {}",
                        topic, orderId, ex.getMessage());
            } else {
                log.info("Published event to topic [{}] partition [{}] offset [{}] for orderId [{}]",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        orderId);
            }
        });

    }

}
