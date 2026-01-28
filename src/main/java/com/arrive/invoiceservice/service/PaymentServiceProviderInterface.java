package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.enums.PaymentProviderResult;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;

public interface PaymentServiceProviderInterface {

    /**
     * Processes a payment using the specified payment entity.
     * Ideally, a dto with the information needed to make the payment should be passed.
     * not the entity
     *
     * @param entity The payment entity containing payment details.
     * @return The result of the payment processing.
     */
    PaymentProviderResult processPayment(PaymentEntity entity);
}
