package com.mritunjay.zomato.oms.dto;

import lombok.Data;

@Data
public class OrderItemRequest {

    private Long itemId;
    private Integer quantity;
    private Double price;

}
