package com.mritunjay.zomato.oms.dto;

import lombok.Data;

@Data
public class PaymentRequestDto {

    private Long orderId;
    private Double amount;
    private String paymentMethod;
    private String idempotencyKey;

}
