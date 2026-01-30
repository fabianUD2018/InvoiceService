package com.arrive.invoiceservice.model.request.paymentprovider;

/**
 * Enum representing the result of a payment processing operation.
 * In a real scenario this should contain more fields and be a dto of the response.
 * To keep it simple, i just let this.
 */
public enum PaymentProviderResult {

    SUCCESS, FAILURE, PENDING_CONFIRMATION
}
