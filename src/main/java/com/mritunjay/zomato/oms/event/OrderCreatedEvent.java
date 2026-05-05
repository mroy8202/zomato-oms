package com.mritunjay.zomato.oms.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private Double totalAmount;

}
