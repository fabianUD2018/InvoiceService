package com.arrive.invoiceservice.mappers;

import com.arrive.invoiceservice.model.request.invoice.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.lineitem.CreateLineItemRequest;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import com.arrive.invoiceservice.model.response.invoice.InvoiceResponse;
import com.arrive.invoiceservice.model.response.payment.PaymentResponse;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.invoice.LineItemEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/*
    Map structs mapper
    This class should have unit test even though is an interface;
    Given time constraints, I won't spend time on this.
 */
@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "total", expression = "java(getTotalPayment(invoiceEntity))")
    InvoiceResponse invoiceEntityToInvoiceResponse(InvoiceEntity invoiceEntity);

    InvoiceEntity createInvoiceRequestToInvoiceEntity(CreateInvoiceRequest invoiceDto);

    LineItemEntity lineItemDtoToEntity(CreateLineItemRequest createLineItemRequest);

    @Mapping(target = "paymentProvider", expression = "java(getPaymentProvider(payInvoiceRequest.getPaymentMethod()))")
    @Mapping(target = "amount", expression = "java(getTotalPayment(invoiceEntity))")
    @Mapping(target = "invoice", source = "invoiceEntity")
    @Mapping(target = "id", ignore = true)
    PaymentEntity invoiceRequestToPaymentEntity(InvoiceEntity invoiceEntity, PayInvoiceRequest payInvoiceRequest);

    @Mapping(target = "paymentId", source = "paymentEntity.id")
    @Mapping(target = "status", expression = "java(paymentEntity.getPaymentStatus().name())")
    PaymentResponse paymentEntityToPaymentResponse(PaymentEntity paymentEntity);

    default BigDecimal getTotalPayment(InvoiceEntity invoice) {
        return invoice.getLineItems()
                .stream().map(LineItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default PaymentProvider getPaymentProvider(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case STRIPE -> PaymentProvider.STRIPE;
            case PAYPAL -> PaymentProvider.PAYPAL;
        };
    }
}
