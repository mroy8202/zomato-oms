package com.mritunjay.zomato.oms.dto.Order;

import com.mritunjay.zomato.oms.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderResponseDTO {

    private Long id;
    private OrderStatus status;
    private Double totalAmount;
    private Long userId;
    private Long restaurantId;
    private Long deliveryPartnerId;
    private List<OrderItemResponseDTO> items;

}
