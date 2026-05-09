package com.mritunjay.zomato.oms.mapper.Order;

import com.mritunjay.zomato.oms.dto.Order.OrderItemRequestDTO;
import com.mritunjay.zomato.oms.dto.Order.OrderItemResponseDTO;
import com.mritunjay.zomato.oms.dto.Order.OrderResponseDTO;
import com.mritunjay.zomato.oms.dto.Order.OrderStatusHistoryResponseDTO;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.model.OrderItem;
import com.mritunjay.zomato.oms.model.OrderStatusHistory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    // Entity -> Response DTO
    public OrderResponseDTO convertOrderEntityToOrderResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .userId(order.getUserId())
                .restaurantId(order.getRestaurantId())
                .deliveryPartnerId(
                        order.getDeliveryPartner() != null
                                ? order.getDeliveryPartner().getId()
                                : null
                )
                .items(mapOrderItems(order.getItems()))
                .statusHistory(mapStatusHistory(order.getStatusHistory()))
                .build();
    }

    private List<OrderItemResponseDTO> mapOrderItems(List<OrderItem> items) {

        if(items == null) return List.of();

        return items.stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .itemId(item.getItemId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()
                ).collect(Collectors.toList());

    }

    private List<OrderStatusHistoryResponseDTO> mapStatusHistory(List<OrderStatusHistory> history) {

        if(history == null) return List.of();

        return history.stream()
                .map(h -> OrderStatusHistoryResponseDTO.builder()
                        .status(h.getStatus())
                        .changedAt(h.getCreatedAt())
                        .build()
                ).collect(Collectors.toList());

    }

    // Request DTO -> Entity
    public List<OrderItem> convertOrderRequestDtoToOrderEntity(List<OrderItemRequestDTO> itemRequestDTOS, Order order) {

        if(itemRequestDTOS == null) return List.of();

        return itemRequestDTOS.stream()
                .map(itemRequestDTO -> OrderItem.builder()
                        .itemId(itemRequestDTO.getItemId())
                        .quantity(itemRequestDTO.getQuantity())
                        .price(itemRequestDTO.getPrice())
                        .order(order)
                        .build()
                ).collect(Collectors.toList());

    }

    public OrderStatusHistory buildStatusHistory(Order order, OrderStatus status) {

        return OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

    }

}
