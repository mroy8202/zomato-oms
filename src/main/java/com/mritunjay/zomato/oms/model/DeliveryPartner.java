package com.mritunjay.zomato.oms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Boolean available;

    private Double latitude;

    private Double longitude;

    // Added @Version for optimistic locking as a safety net alongside
    // the pessimistic lock in DeliveryPartnerRepository
    @Version
    @Builder.Default
    private Integer version = 0;

}
