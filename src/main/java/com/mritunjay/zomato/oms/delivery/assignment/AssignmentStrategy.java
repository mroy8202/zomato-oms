package com.mritunjay.zomato.oms.delivery.assignment;

import com.mritunjay.zomato.oms.model.DeliveryPartner;

import java.util.List;

public interface AssignmentStrategy {

    DeliveryPartner assign(List<DeliveryPartner> partners);

}
