package com.mritunjay.zomato.oms.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topic.payment-success}")
    private String paymentSuccessTopic;

    @Bean
    public NewTopic orderCreatedTopic() {

        return TopicBuilder.name(orderCreatedTopic)
                .partitions(3)
                .replicas(1)
                .build();

    }

    @Bean
    public  NewTopic paymentSuccessTopic() {

        return TopicBuilder.name(paymentSuccessTopic)
                .partitions(3)
                .replicas(1)
                .build();

    }

}
