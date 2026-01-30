package com.arrive.invoiceservice.model.exceptions;

public class InvoicePaymentStateException extends RuntimeException {
    public InvoicePaymentStateException(String message) {
        super(message);
    }
}
