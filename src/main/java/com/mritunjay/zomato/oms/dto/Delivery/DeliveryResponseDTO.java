package com.mritunjay.zomato.oms.dto.Delivery;

import com.mritunjay.zomato.oms.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryResponseDTO {

    private Long orderId;
    private Long deliveryPartnerId;
    private OrderStatus status;

}
