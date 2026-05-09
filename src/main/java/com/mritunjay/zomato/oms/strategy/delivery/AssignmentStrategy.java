package com.mritunjay.zomato.oms.strategy.delivery;

import com.mritunjay.zomato.oms.model.DeliveryPartner;

import java.util.List;

public interface AssignmentStrategy {

    DeliveryPartner assign(List<DeliveryPartner> partners);

}
