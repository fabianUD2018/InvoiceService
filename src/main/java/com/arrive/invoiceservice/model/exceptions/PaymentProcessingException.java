package com.arrive.invoiceservice.model.exceptions;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message, Exception exception) {
        super(message, exception);
    }
}
