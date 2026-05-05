package com.mritunjay.zomato.oms.service;

import com.mritunjay.zomato.oms.dto.Payment.PaymentRequestDTO;
import com.mritunjay.zomato.oms.dto.Payment.PaymentResponseDTO;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.enums.PaymentStatus;
import com.mritunjay.zomato.oms.event.PaymentSuccessEvent;
import com.mritunjay.zomato.oms.exception.InvalidStateTransitionException;
import com.mritunjay.zomato.oms.exception.OrderNotFoundException;
import com.mritunjay.zomato.oms.mapper.Payment.PaymentMapper;
import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.model.Payment;
import com.mritunjay.zomato.oms.publisher.EventPublisher;
import com.mritunjay.zomato.oms.repository.OrderRepository;
import com.mritunjay.zomato.oms.repository.PaymentRepository;
import com.mritunjay.zomato.oms.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {

        log.info("Processing payment for order {}", request.getOrderId());

        // Idempotency check — return existing result if already processed
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if(existing.isPresent()) {
            log.info("Idempotent hit for key {}", request.getIdempotencyKey());
            return paymentMapper.convertPaymentEntityToPaymentResponseDto(existing.get());
        }

        // Fetch order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setStatus(PaymentStatus.SUCCESS);

        paymentRepository.save(payment);

        // Update order status using state machine
        if(payment.getStatus() == PaymentStatus.SUCCESS) {
            if(OrderStateMachine.cannotTransition(order.getStatus(), OrderStatus.CONFIRMED)) {
                throw new InvalidStateTransitionException(order.getStatus(), OrderStatus.CONFIRMED);
            }

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order {} confirmed after payment success", order.getId());

            // trigger delivery assignment
            eventPublisher.publish(new PaymentSuccessEvent(order.getId()));
        }

        return paymentMapper.convertPaymentEntityToPaymentResponseDto(payment);
    }

}
