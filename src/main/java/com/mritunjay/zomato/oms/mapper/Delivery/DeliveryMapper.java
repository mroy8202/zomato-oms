package com.mritunjay.zomato.oms.mapper.Delivery;

import com.mritunjay.zomato.oms.dto.Delivery.DeliveryResponseDTO;
import com.mritunjay.zomato.oms.model.Order;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMapper {

    // Entity -> Response DTO
    public DeliveryResponseDTO convertOrderEntityToDeliveryResponseDto(Order order) {

        return DeliveryResponseDTO.builder()
                .orderId(order.getId())
                .deliveryPartnerId(
                        order.getDeliveryPartner() != null
                                ? order.getDeliveryPartner().getId()
                                : null
                )
                .status(order.getStatus())
                .build();

    }

}
