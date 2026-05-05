package com.mritunjay.zomato.oms.dto.Order;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequestDTO {

    private Long userId;
    private Long restaurantId;
    private List<OrderItemRequestDTO> items;

}
