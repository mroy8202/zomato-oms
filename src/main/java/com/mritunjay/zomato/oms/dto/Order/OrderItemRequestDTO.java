package com.mritunjay.zomato.oms.dto.Order;

import lombok.Data;

@Data
public class OrderItemRequestDTO {

    private Long itemId;
    private Integer quantity;
    private Double price;

}
