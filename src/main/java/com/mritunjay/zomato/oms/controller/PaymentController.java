package com.mritunjay.zomato.oms.controller;

import com.mritunjay.zomato.oms.dto.PaymentRequestDto;
import com.mritunjay.zomato.oms.model.Payment;
import com.mritunjay.zomato.oms.service.PaymentService;
import lombok.RequiredArgsConstructor;
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
    public Payment process(@RequestBody PaymentRequestDto request) {

        return paymentService.processPayment(request);

    }

}
