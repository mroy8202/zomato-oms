package com.mritunjay.zomato.oms.mapper.Payment;

import com.mritunjay.zomato.oms.dto.Payment.PaymentResponseDTO;
import com.mritunjay.zomato.oms.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    // Entity -> Response DTO
    public PaymentResponseDTO convertPaymentEntityToPaymentResponseDto(Payment payment) {

        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .orderId(payment.getOrder().getId())
                .build();

    }

}
