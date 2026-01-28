package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.enums.PaymentProviderResult;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PaypalPaymentProvider implements PaymentServiceProviderInterface {

    @Override
    public PaymentProviderResult processPayment(PaymentEntity entity) {
        log.info("Processing payment with paypal - mocking response with success");
        return PaymentProviderResult.SUCCESS;
    }
}
