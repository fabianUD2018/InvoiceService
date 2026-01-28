package com.arrive.invoiceservice.model.request.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayInvoiceRequest {

    //todo: In a real scenario one should consider protection like a csrf token
    //skipping this for now

    private PaymentMethod paymentMethod;

}
