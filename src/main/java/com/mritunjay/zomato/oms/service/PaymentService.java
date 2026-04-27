package com.mritunjay.zomato.oms.service;

import com.mritunjay.zomato.oms.dto.PaymentRequestDto;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.enums.PaymentStatus;
import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.model.Payment;
import com.mritunjay.zomato.oms.repository.OrderRepository;
import com.mritunjay.zomato.oms.repository.PaymentRepository;
import com.mritunjay.zomato.oms.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Payment processPayment(PaymentRequestDto request) {

        // check idempotency
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if(existing.isPresent()) {
            return existing.get(); // returns old result (no duplicate)
        }

        // Fetch order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Create payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setIdempotencyKey(request.getIdempotencyKey());

        paymentRepository.save(payment);

        // Update order status using state machine
        if(OrderStateMachine.canTransition(order.getStatus(), OrderStatus.CONFIRMED)) {
            order.setStatus(OrderStatus.CONFIRMED);
        }

        return payment;
    }

}
