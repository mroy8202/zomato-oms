package com.mritunjay.zomato.oms.controller;

import com.mritunjay.zomato.oms.model.Order;
import com.mritunjay.zomato.oms.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/assign/{orderId}")
    public Order assign(@PathVariable Long orderId) {
        return deliveryService.assignPartner(orderId);
    }

}
