package com.mritunjay.zomato.oms.controller;

import com.mritunjay.zomato.oms.dto.Payment.PaymentRequestDTO;
import com.mritunjay.zomato.oms.dto.Payment.PaymentResponseDTO;
import com.mritunjay.zomato.oms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> process(@RequestBody PaymentRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.processPayment(request));

    }

}
