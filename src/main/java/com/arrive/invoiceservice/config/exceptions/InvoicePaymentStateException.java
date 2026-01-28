package com.arrive.invoiceservice.config.exceptions;

public class InvoicePaymentStateException extends RuntimeException {
    public InvoicePaymentStateException(String message) {
        super(message);
    }
}
