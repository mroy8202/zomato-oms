package com.mritunjay.zomato.oms.repository;

import com.mritunjay.zomato.oms.model.DeliveryPartner;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT  p FROM DeliveryPartner p WHERE p.available = true")
    List<DeliveryPartner> findAvailableForUpdate();

}
