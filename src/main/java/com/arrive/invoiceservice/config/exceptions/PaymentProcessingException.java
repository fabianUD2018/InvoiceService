package com.arrive.invoiceservice.config.exceptions;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message, Exception exception) {
        super(message, exception);
    }
}
