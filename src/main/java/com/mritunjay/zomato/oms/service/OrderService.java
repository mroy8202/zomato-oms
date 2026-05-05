package com.mritunjay.zomato.oms.service;

import com.mritunjay.zomato.oms.dto.Order.CreateOrderRequestDTO;
import com.mritunjay.zomato.oms.dto.Order.OrderResponseDTO;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.event.OrderCreatedEvent;
import com.mritunjay.zomato.oms.exception.InvalidStateTransitionException;
import com.mritunjay.zomato.oms.exception.OrderNotFoundException;
import com.mritunjay.zomato.oms.mapper.Order.OrderMapper;
import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.model.OrderItem;
import com.mritunjay.zomato.oms.publisher.EventPublisher;
import com.mritunjay.zomato.oms.repository.OrderRepository;
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
    private final EventPublisher eventPublisher;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request) {

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

        // Transition to PAYMENT_PENDING before triggering payment
        savedOrder.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(savedOrder);

        log.info("Order {} created, status set to PAYMENT_PENDING", savedOrder.getId());

        // publish event
        eventPublisher.publish(new OrderCreatedEvent(savedOrder.getId(), savedOrder.getTotalAmount()));

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
        log.info("Order {} status updated to {}", orderId, newStatus);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.convertOrderEntityToOrderResponseDTO(savedOrder);

    }

}
