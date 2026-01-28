package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.enums.PaymentProviderResult;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import org.springframework.stereotype.Service;

@Service
public class PaypalPaymentProvider implements PaymentServiceProviderInterface {

    @Override
    public PaymentProviderResult processPayment(PaymentEntity entity) {
        return PaymentProviderResult.SUCCESS;
    }
}
