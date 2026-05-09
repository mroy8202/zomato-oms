package com.mritunjay.zomato.oms.strategy.delivery;

import com.mritunjay.zomato.oms.exception.NoDeliveryPartnerAvailableException;
import com.mritunjay.zomato.oms.model.DeliveryPartner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RandomAssignmentStrategy implements AssignmentStrategy {

    private final Random random = new Random();

    @Override
    public DeliveryPartner assign(List<DeliveryPartner> partners) {

        if(partners.isEmpty()) {
            throw new NoDeliveryPartnerAvailableException();
        }
        return partners.get(random.nextInt(partners.size()));

    }

}
