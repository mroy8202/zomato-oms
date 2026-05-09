package com.mritunjay.zomato.oms.service;

import com.mritunjay.zomato.oms.dto.Payment.PaymentRequestDTO;
import com.mritunjay.zomato.oms.dto.Payment.PaymentResponseDTO;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.enums.PaymentStatus;
import com.mritunjay.zomato.oms.event.PaymentSuccessEvent;
import com.mritunjay.zomato.oms.exception.InvalidStateTransitionException;
import com.mritunjay.zomato.oms.exception.OrderNotFoundException;
import com.mritunjay.zomato.oms.kafka.producer.KafkaEventPublisher;
import com.mritunjay.zomato.oms.mapper.Payment.PaymentMapper;
import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.model.Payment;
import com.mritunjay.zomato.oms.repository.OrderRepository;
import com.mritunjay.zomato.oms.repository.PaymentRepository;
import com.mritunjay.zomato.oms.statemachine.OrderStateMachine;
import com.mritunjay.zomato.oms.strategy.payment.PaymentStrategy;
import com.mritunjay.zomato.oms.strategy.payment.PaymentStrategyFactory;
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
    private final KafkaEventPublisher kafkaEventPublisher;
    private final PaymentMapper paymentMapper;
    private final PaymentStrategyFactory paymentStrategyFactory;

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {

        log.info("Processing payment for orderId [{}] via [{}]", request.getOrderId(), request.getPaymentMethod());

        // Idempotency check :
        // If this exact idempotency key was already processed, return cached result.
        // Prevents duplicate payments even if Kafka re-delivers the message.
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if(existing.isPresent()) {
            log.info("Idempotent hit for key [{}] — returning existing payment", request.getIdempotencyKey());
            return paymentMapper.convertPaymentEntityToPaymentResponseDto(existing.get());
        }

        // Fetch order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));

        // Resolve and execute payment strategy
        /// UPI → UpiPaymentStrategy, CARD → CardPaymentStrategy, etc.
        PaymentStrategy strategy = paymentStrategyFactory.resolve(request.getPaymentMethod());
        boolean success = strategy.process(request.getAmount());

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        paymentRepository.save(payment);

        if(payment.getStatus() == PaymentStatus.SUCCESS) {

            // Validate state transition: PAYMENT_PENDING → CONFIRMED
            if(OrderStateMachine.cannotTransition(order.getStatus(), OrderStatus.CONFIRMED)) {
                throw new InvalidStateTransitionException(order.getStatus(), OrderStatus.CONFIRMED);
            }

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order [{}] confirmed after successful payment", order.getId());

            // Publish to Kafka "payment-success" topic
            // DeliveryKafkaConsumer will pick this up and assign a partner
            kafkaEventPublisher.publishPaymentSuccess(
                    new PaymentSuccessEvent(order.getId()),
                    order.getId()
            );
        }

        return paymentMapper.convertPaymentEntityToPaymentResponseDto(payment);
    }

}
