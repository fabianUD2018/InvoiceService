package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.model.request.paymentprovider.PaymentProviderResult;
import com.arrive.invoiceservice.model.request.paymentprovider.PaymentProviderRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class StripePaymentProvider implements PaymentServiceProviderInterface {

    @Override
    public PaymentProviderResult processPayment(PaymentProviderRequest paymentProviderRequest) {
        log.info("Mocking Processing payment with stripe and it fails");
        return PaymentProviderResult.FAILURE;
    }
}
