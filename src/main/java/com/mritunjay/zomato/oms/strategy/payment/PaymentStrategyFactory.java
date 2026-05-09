package com.mritunjay.zomato.oms.strategy.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategyMap;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {

        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getMethod().toUpperCase(),
                        Function.identity()
                ));
        log.info("Registered payment strategies: {}", strategyMap.keySet());

    }

    public PaymentStrategy resolve(String method) {

        PaymentStrategy strategy = strategyMap.get(method.toUpperCase());
        if(strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method + ". Available: " + strategyMap.keySet());
        }
        return strategy;

    }

}
