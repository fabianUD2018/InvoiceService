package com.arrive.invoiceservice.utils;

import com.arrive.invoiceservice.model.request.invoice.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.lineitem.CreateLineItemRequest;
import com.arrive.invoiceservice.model.request.lineitem.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import com.arrive.invoiceservice.model.response.lineitem.LineItemResponse;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.invoice.LineItemEntity;
import com.arrive.invoiceservice.repository.entity.invoice.ProductEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class InvoiceUtils {

    public static final Instant DEFAULT_CREATED_DATE = Instant.now();

    public static InvoiceEntity createRandomInvoiceEntity() {
        ArrayList<LineItemEntity> lineItems = new ArrayList<>();
        lineItems.add(createLineItemEntity("shoes", 1, BigDecimal.valueOf(100.0), "random product"));
        return InvoiceEntity.builder()
                .createdDate(DEFAULT_CREATED_DATE)
                .id(UUID.randomUUID())
                .lineItems(lineItems)
                .payments(new ArrayList<>())
                .build();
    }

    public static CreateInvoiceRequest createInvoiceRequest(String sku, Integer quantity, BigDecimal price) {
        return CreateInvoiceRequest.builder()
                .lineItems(List.of(CreateLineItemRequest.builder()
                        .sku(sku)
                        .quantity(quantity)
                        .unitPrice(price)
                        .build()))
                .build();
    }

    public static PatchLineItemsRequest createPatchLineItemRequest(String sku, Integer quantity, BigDecimal price) {
        return PatchLineItemsRequest.builder()
                .lineItems(List.of(CreateLineItemRequest.builder()
                        .sku(sku)
                        .quantity(quantity)
                        .unitPrice(price)
                        .build()))
                .build();
    }

    public static CreateInvoiceRequest createRandomInvoiceRequest() {
        return CreateInvoiceRequest.builder()
                .lineItems(createRandomLineItemRequest()).build();
    }

    public static LineItemEntity createLineItemEntity(String sku, int quantity, BigDecimal price, String productDescription) {
        return LineItemEntity
                .builder()
                .product(ProductEntity.builder().sku(sku).description(productDescription).build())
                .quantity(quantity)
                .unitPrice(price)
                .build();
    }

    public static List<CreateLineItemRequest> createRandomLineItemRequest() {
        return List.of(CreateLineItemRequest.builder()
                .unitPrice(BigDecimal.valueOf(100.0))
                .sku("shoes")
                .quantity(1)
                .build()
        );
    }

    public static PatchLineItemsRequest createRandomPatchLineItemRequest() {
        return PatchLineItemsRequest.builder().lineItems(createRandomLineItemRequest()).build();
    }

    public static PayInvoiceRequest createPayRequest(PaymentMethod paymentMethod) {
        return PayInvoiceRequest.builder().paymentMethod(paymentMethod).build();
    }

    public static PayInvoiceRequest createPayRequest(String paymentMethod) {
        return PayInvoiceRequest.builder().paymentMethod(paymentMethod != null ? PaymentMethod.valueOf(paymentMethod) : null).build();
    }

    public static PaymentEntity createRandomPaymentEntity(PaymentStatus status) {
        return PaymentEntity.builder()
                .id(UUID.randomUUID())
                .paymentStatus(status)
                .amount(BigDecimal.valueOf(100.0))
                .build();
    }

    public static LineItemResponse createLineItemResponse(String sku, int quantity, BigDecimal price) {
        return new LineItemResponse(sku, quantity, price);
    }
}
