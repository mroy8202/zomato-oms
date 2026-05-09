package com.mritunjay.zomato.oms.strategy.delivery;

import com.mritunjay.zomato.oms.exception.NoDeliveryPartnerAvailableException;
import com.mritunjay.zomato.oms.model.DeliveryPartner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Primary
public class NearestAssignmentStrategy implements AssignmentStrategy {

    // Placeholder restaurant location - In production, pass via method parameter
    private static final double RESTAURANT_LAT = 12.9716;
    private static final double RESTAURANT_LON = 77.5946;
    private static final int EARTH_RADIUS_KM = 6371;

    @Override
    public DeliveryPartner assign(List<DeliveryPartner> partners) {

        if(partners.isEmpty()) {
            throw new NoDeliveryPartnerAvailableException();
        }

        DeliveryPartner nearest = null;
        double minDistance = Double.MAX_VALUE;

        for(DeliveryPartner partner: partners) {
            Double distance = haversine(
                    partner.getLatitude(), partner.getLongitude()
            );
            log.info("Partner {} → distance: {} km", partner.getId(), String.format("%.2f", distance));
            if(distance < minDistance) {
                minDistance = distance;
                nearest = partner;
            }
        }

        log.info("Nearest partner: {} at {} km", nearest.getId(), String.format("%.2f", minDistance));
        return nearest;

    }

    private Double haversine(double lat2, double lon2) {

        double dLat = Math.toRadians(lat2 - NearestAssignmentStrategy.RESTAURANT_LAT);
        double dLon = Math.toRadians(lon2 - NearestAssignmentStrategy.RESTAURANT_LON);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(NearestAssignmentStrategy.RESTAURANT_LAT)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    }

}
