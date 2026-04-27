package com.mritunjay.zomato.oms.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    private Long userId;
    private Long restaurantId;
    private List<OrderItemRequest> items;

}
