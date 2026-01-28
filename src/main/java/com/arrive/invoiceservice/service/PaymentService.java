package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import com.arrive.invoiceservice.repository.PaymentRepository;
import com.arrive.invoiceservice.repository.entity.invoice.LineItemEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentProvider;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final PaymentProviderFactory paymentProviderFactory;

    @Transactional
    public UUID processPayment(UUID invoiceId, PayInvoiceRequest request) {
        var invoice = invoiceService.getInvoiceEntity(invoiceId);
        BigDecimal totalPayment = invoice.getLineItems()
                .stream().map(LineItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        PaymentEntity payment = PaymentEntity.builder()
                .paymentProvider(getPaymentProvider(request.getPaymentMethod()))
                .invoice(invoice)
                .amount(totalPayment)
                .build();

        var paymentStatus = paymentProviderFactory
                .getPaymentProvider(request.getPaymentMethod())
                .processPayment(payment);

        switch (paymentStatus) {
            case SUCCESS -> payment.setPaymentStatus(PaymentStatus.PAID);
            case FAILURE -> payment.setPaymentStatus(PaymentStatus.FAILED);
            case PENDING_RESPONSE -> payment.setPaymentStatus(PaymentStatus.PENDING);
        }
        paymentRepository.save(payment);
        return payment.getId();
    }

    public PaymentProvider getPaymentProvider(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case STRIPE -> PaymentProvider.STRIPE;
            case PAYPAL -> PaymentProvider.PAYPAL;
        };
    }

}
