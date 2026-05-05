package com.mritunjay.zomato.oms.dto.Order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponseDTO {

    private Long itemId;
    private Integer quantity;
    private Double price;

}
