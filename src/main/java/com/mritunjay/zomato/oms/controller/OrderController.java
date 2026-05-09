package com.mritunjay.zomato.oms.controller;

import com.mritunjay.zomato.oms.dto.Order.CreateOrderRequestDTO;
import com.mritunjay.zomato.oms.dto.Order.OrderResponseDTO;
import com.mritunjay.zomato.oms.enums.OrderStatus;
import com.mritunjay.zomato.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /orders — create a new order
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody CreateOrderRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));

    }

    // GET /orders/{id} — get order by id (includes history)
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {

        return ResponseEntity
                .ok(orderService.getOrder(id));

    }

    // DELETE /orders/{id}/cancel — cancel an order
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {

        return ResponseEntity
                .ok(orderService.cancelOrder(id));

    }

    // PUT /orders/{id}/status — manually update status (admin/internal use)
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Long id,
                              @RequestParam OrderStatus status) {

        return ResponseEntity
                .ok(orderService.updateStatus(id, status));
    }

}
