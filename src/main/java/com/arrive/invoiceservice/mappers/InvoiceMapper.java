package com.arrive.invoiceservice.mappers;

import com.arrive.invoiceservice.model.request.lineitem.CreateLineItemRequest;
import com.arrive.invoiceservice.model.request.paymentprovider.PaymentProviderRequest;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import com.arrive.invoiceservice.model.response.invoice.InvoiceResponse;
import com.arrive.invoiceservice.model.response.lineitem.LineItemResponse;
import com.arrive.invoiceservice.model.response.payment.PaymentResponse;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.invoice.LineItemEntity;
import com.arrive.invoiceservice.repository.entity.invoice.ProductEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentProvider;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = PaymentStatus.class)
public interface InvoiceMapper {

    @Mapping(target = "total", expression = "java(invoiceEntity.getTotalPayment())")
    InvoiceResponse invoiceEntityToInvoiceResponse(InvoiceEntity invoiceEntity);

    @Mapping(target = "sku", source = "lineItemEntity.product.sku")
    LineItemResponse lineItemEntityToLineItemResponse(LineItemEntity lineItemEntity);

    @Mapping(target = "product", source = "productEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    LineItemEntity createLineItemRequestToLineItemEntity(CreateLineItemRequest createLineItemRequest, ProductEntity productEntity);

    @Mapping(target = "paymentProvider", expression = "java(getPaymentProvider(payInvoiceRequest.getPaymentMethod()))")
    @Mapping(target = "amount", expression = "java(invoiceEntity.getTotalPayment())")
    @Mapping(target = "invoice", source = "invoiceEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentStatus", expression = "java(PaymentStatus.INITIATED)")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "paidDate", ignore = true)
    PaymentEntity invoiceRequestToPaymentEntity(InvoiceEntity invoiceEntity, PayInvoiceRequest payInvoiceRequest);

    @Mapping(target = "paymentId", source = "paymentEntity.id")
    @Mapping(target = "status", expression = "java(paymentEntity.getPaymentStatus().name())")
    PaymentResponse paymentEntityToPaymentResponse(PaymentEntity paymentEntity);

    default PaymentProvider getPaymentProvider(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case STRIPE -> PaymentProvider.STRIPE;
            case PAYPAL -> PaymentProvider.PAYPAL;
        };
    }

    @Mapping(target = "amountToPay", source = "payment.amount")
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "currency", ignore = true)
    PaymentProviderRequest paymentEntityToPaymentProviderRequest(PaymentEntity payment);
}
