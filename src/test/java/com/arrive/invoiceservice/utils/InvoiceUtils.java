package com.arrive.invoiceservice.utils;

import com.arrive.invoiceservice.model.request.invoice.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.lineitem.CreateLineItemRequest;
import com.arrive.invoiceservice.model.request.lineitem.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.invoice.LineItemEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class InvoiceUtils {

    public static InvoiceEntity createRandomInvoiceEntity() {
        ArrayList<LineItemEntity> lineItems = new ArrayList<>();
        lineItems.add(createLineItemEntity("Random Line Item", BigDecimal.valueOf(100.0)));
        return InvoiceEntity.builder()
                .id(UUID.randomUUID())
                .lineItems(lineItems)
                .payments(new ArrayList<>())
                .build();
    }

    public static CreateInvoiceRequest createRandomInvoiceRequest() {
        return CreateInvoiceRequest.builder()
                .lineItems(createRandomLineItemRequest()).build();
    }

    public static LineItemEntity createLineItemEntity(String description, BigDecimal price) {
        return LineItemEntity.builder().description(description).price(price).build();
    }

    public static List<CreateLineItemRequest> createRandomLineItemRequest() {
        return List.of(CreateLineItemRequest.builder()
                .price(BigDecimal.valueOf(100.0))
                .description("Random Line Item")
                .build());
    }

    public static PatchLineItemsRequest createRandomPatchLineItemRequest() {
        return PatchLineItemsRequest.builder().lineItems(createRandomLineItemRequest()).build();
    }

    public static PayInvoiceRequest createPayRequest(PaymentMethod paymentMethod) {
        return PayInvoiceRequest.builder().paymentMethod(paymentMethod).build();
    }

    public static PaymentEntity createRandomPaymentEntity(PaymentStatus status) {
        return PaymentEntity.builder()
                .paymentStatus(status)
                .build();
    }
}
