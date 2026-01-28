package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.enums.PaymentProviderResult;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class StripePaymentProvider implements PaymentServiceProviderInterface {

    @Override
    public PaymentProviderResult processPayment(PaymentEntity entity) {
        log.info("Mocking Processing payment with stripe and it fails");
        return PaymentProviderResult.FAILURE;
    }
}
