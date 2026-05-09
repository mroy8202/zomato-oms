package com.mritunjay.zomato.oms.service;

import com.mritunjay.zomato.oms.dto.Order.CreateOrderRequestDTO;
import com.mritunjay.zomato.oms.dto.Order.OrderResponseDTO;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.event.OrderCreatedEvent;
import com.mritunjay.zomato.oms.exception.InvalidStateTransitionException;
import com.mritunjay.zomato.oms.exception.OrderNotFoundException;
import com.mritunjay.zomato.oms.kafka.producer.KafkaEventPublisher;
import com.mritunjay.zomato.oms.mapper.Order.OrderMapper;
import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.model.OrderItem;
import com.mritunjay.zomato.oms.model.OrderStatusHistory;
import com.mritunjay.zomato.oms.repository.OrderRepository;
import com.mritunjay.zomato.oms.repository.OrderStatusHistoryRepository;
import com.mritunjay.zomato.oms.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request) {

        // Build order entity
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setRestaurantId(request.getRestaurantId());
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> items = orderMapper.convertOrderRequestDtoToOrderEntity(request.getItems(), order);
        order.setItems(items);

        double total= items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        recordHistory(savedOrder, OrderStatus.CREATED);

        // Move to PAYMENT_PENDING — state machine requires this before CONFIRMED
        savedOrder.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(savedOrder);
        recordHistory(savedOrder, OrderStatus.PAYMENT_PENDING);

        log.info("Order {} created with total ₹{}, status PAYMENT_PENDING", savedOrder.getId(), savedOrder.getTotalAmount());

        // Publish to Kafka "order-created" topic (replaces Spring ApplicationEvent)
        // Key = orderId → ensures all events for this order go to same partition (ordered)
        kafkaEventPublisher.publishOrderCreated(
                new OrderCreatedEvent(savedOrder.getId(), savedOrder.getTotalAmount()),
                savedOrder.getId()
        );

        return orderMapper.convertOrderEntityToOrderResponseDTO(savedOrder);

    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return orderMapper.convertOrderEntityToOrderResponseDTO(order);

    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(OrderStateMachine.cannotTransition(order.getStatus(), OrderStatus.CANCELLED)) {
            throw new InvalidStateTransitionException(order.getStatus(), OrderStatus.CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        recordHistory(savedOrder, OrderStatus.CANCELLED);

        log.info("Order {} cancelled", orderId);
        return orderMapper.convertOrderEntityToOrderResponseDTO(savedOrder);
    }

    @Transactional
    public OrderResponseDTO updateStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(OrderStateMachine.cannotTransition(order.getStatus(), newStatus)) {
            throw new InvalidStateTransitionException(order.getStatus(), newStatus);
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        recordHistory(savedOrder, newStatus);

        log.info("Order {} manually updated to status {}", orderId, newStatus);
        return orderMapper.convertOrderEntityToOrderResponseDTO(savedOrder);

    }

    private void recordHistory(Order order, OrderStatus status) {

        OrderStatusHistory history = orderMapper.buildStatusHistory(order, status);
        historyRepository.save(history);

    }

}
