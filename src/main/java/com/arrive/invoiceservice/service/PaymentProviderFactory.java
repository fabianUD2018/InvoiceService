package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentProviderFactory {

    private final PaypalPaymentProvider paypalPaymentProvider;

    private final StripePaymentProvider stripePaymentProvider;

    public PaymentServiceProviderInterface getPaymentProvider(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case PAYPAL -> paypalPaymentProvider;
            case STRIPE -> stripePaymentProvider;
        };
    }


}
