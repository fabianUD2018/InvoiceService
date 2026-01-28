package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.mappers.InvoiceMapper;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.response.payment.PaymentResponse;
import com.arrive.invoiceservice.repository.PaymentRepository;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PaymentService {
    private final InvoiceMapper invoiceMapper;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final PaymentProviderFactory paymentProviderFactory;

    public PaymentResponse processPayment(UUID invoiceId, PayInvoiceRequest request) {
        var invoice = invoiceService.getInvoiceEntity(invoiceId);
        invoiceService.verifyInvoiceState(invoice);

        PaymentEntity payment = invoiceMapper.invoiceRequestToPaymentEntity(invoice, request);

        var paymentStatus = paymentProviderFactory
                .getPaymentProvider(request.getPaymentMethod())
                .processPayment(payment);

        switch (paymentStatus) {
            case SUCCESS ->  {
                payment.setPaidDate(Instant.now());
                payment.setPaymentStatus(PaymentStatus.PAID);
            }
            case FAILURE -> payment.setPaymentStatus(PaymentStatus.FAILED);
            case PENDING_CONFIRMATION -> payment.setPaymentStatus(PaymentStatus.PENDING);
        }
        return invoiceMapper.paymentEntityToPaymentResponse(paymentRepository.save(payment));
    }

}
