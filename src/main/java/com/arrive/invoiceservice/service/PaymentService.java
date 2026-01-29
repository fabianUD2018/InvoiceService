package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.config.exceptions.PaymentProcessingException;
import com.arrive.invoiceservice.enums.PaymentProviderResult;
import com.arrive.invoiceservice.mappers.InvoiceMapper;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.response.payment.PaymentResponse;
import com.arrive.invoiceservice.repository.PaymentRepository;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Log4j2
public class PaymentService {
    private final InvoiceMapper invoiceMapper;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final PaymentProviderFactory paymentProviderFactory;

    public PaymentResponse processPayment(UUID invoiceId, PayInvoiceRequest request) {
        var invoice = invoiceService.getInvoiceEntity(invoiceId);
        PaymentEntity payment = initiatePayment(request, invoice);
        PaymentProviderResult newPaymentStatus = paymentProviderFactory
                .getPaymentProvider(request.getPaymentMethod())
                .processPayment(payment);
        setPaymentStatus(newPaymentStatus, payment);
        return invoiceMapper.paymentEntityToPaymentResponse(paymentRepository.save(payment));
    }

    private PaymentEntity initiatePayment(PayInvoiceRequest request, InvoiceEntity invoice) {
        try {
            PaymentEntity payment = invoiceMapper.invoiceRequestToPaymentEntity(invoice, request);
            return paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            PaymentEntity currentPayment = paymentRepository.findByInvoiceIdAndPaymentStatusIn(invoice.getId(), Set.of(PaymentStatus.INITIATED, PaymentStatus.PENDING, PaymentStatus.PAID));
            throw new PaymentProcessingException("Payment is in %s for invoice: %s".formatted(currentPayment.getPaymentStatus(), invoice.getId()), e);
        }
    }

    private void setPaymentStatus(PaymentProviderResult paymentStatus, PaymentEntity payment) {
        switch (paymentStatus) {
            case SUCCESS ->  {
                payment.setPaidDate(Instant.now());
                payment.setPaymentStatus(PaymentStatus.PAID);
            }
            case FAILURE -> payment.setPaymentStatus(PaymentStatus.FAILED);
            case PENDING_CONFIRMATION -> payment.setPaymentStatus(PaymentStatus.PENDING);
        }
    }

}
