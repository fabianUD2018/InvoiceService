package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.model.request.paymentprovider.PaymentProviderResult;
import com.arrive.invoiceservice.model.request.paymentprovider.PaymentProviderRequest;

public interface PaymentServiceProviderInterface {

    /**
     * Processes a payment using the specified payment.
     * @param paymentProviderRequest The payment containing payment details.
     * @return The result of the payment processing.
     */
    PaymentProviderResult processPayment(PaymentProviderRequest paymentProviderRequest);
}
