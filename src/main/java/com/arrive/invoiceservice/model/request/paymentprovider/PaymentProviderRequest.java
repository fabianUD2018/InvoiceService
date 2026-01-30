package com.arrive.invoiceservice.model.request.paymentprovider;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentProviderRequest {

    private String amountToPay;

    /*
     * Using usd as currency for simplicity
     */
    @Builder.Default
    private String currency = "USD";

    /*
     * Payment provider specific identifier for the payment
     * This can serve as idempotency key to prevent duplicate payments, in case a payment needs reprocessing for example
     */
    private String paymentId;

}
