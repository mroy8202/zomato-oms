package com.mritunjay.zomato.oms.dto.Payment;

import lombok.Data;

@Data
public class PaymentRequestDTO {

    private Long orderId;
    private Double amount;
    private String paymentMethod;
    private String idempotencyKey;

}
