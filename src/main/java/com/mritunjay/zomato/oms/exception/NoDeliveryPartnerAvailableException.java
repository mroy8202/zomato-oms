package com.mritunjay.zomato.oms.exception;

public class NoDeliveryPartnerAvailableException extends RuntimeException {

    public NoDeliveryPartnerAvailableException() {
        super("No delivery partners are currently available");
    }

}
