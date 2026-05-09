package com.mritunjay.zomato.oms.dto.Order;

import com.mritunjay.zomato.oms.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderStatusHistoryResponseDTO {

    private OrderStatus status;
    private LocalDateTime changedAt;

}
