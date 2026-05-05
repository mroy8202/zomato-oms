package com.mritunjay.zomato.oms.dto.Payment;

import com.mritunjay.zomato.oms.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDTO {

    private Long id;
    private Double amount;
    private String paymentMethod;
    private PaymentStatus status;
    private Long orderId;

}
