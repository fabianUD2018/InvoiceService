package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.enums.PaymentProviderResult;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;

public interface PaymentServiceProviderInterface  {
    PaymentProviderResult processPayment(PaymentEntity entity);
}
