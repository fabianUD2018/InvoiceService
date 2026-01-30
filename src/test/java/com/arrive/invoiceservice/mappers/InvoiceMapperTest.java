package com.arrive.invoiceservice.mappers;

import com.arrive.invoiceservice.model.request.lineitem.CreateLineItemRequest;
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
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static com.arrive.invoiceservice.utils.InvoiceUtils.DEFAULT_CREATED_DATE;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createLineItemEntity;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createLineItemResponse;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomInvoiceEntity;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomPaymentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceMapperTest {

    private final InvoiceMapper mapper = Mappers.getMapper(InvoiceMapper.class);

    @Test
    void invoiceEntityToInvoiceResponse_shouldReturnExpectedResponse() {
        LineItemEntity itemOne  = createLineItemEntity("skuOne", 2, new BigDecimal("10"), "random product");
        LineItemEntity itemTwo  = createLineItemEntity("skuTwo", 1, new BigDecimal("5"), "random product");

        InvoiceEntity invoiceEntity = createRandomInvoiceEntity();
        invoiceEntity.setLineItems(List.of(itemOne, itemTwo));

        InvoiceResponse response = mapper.invoiceEntityToInvoiceResponse(invoiceEntity);
        assertThat(response)
                .extracting(InvoiceResponse::getLineItems, InvoiceResponse::getCreatedDate, InvoiceResponse::getId, InvoiceResponse::getTotal)
                .containsExactly(
                        List.of(createLineItemResponse("skuOne", 2, new BigDecimal("10")), createLineItemResponse("skuTwo", 1, new BigDecimal("5"))),
                        DEFAULT_CREATED_DATE, invoiceEntity.getId(), new BigDecimal("25"));
    }

    @Test
    void lineItemEntityToLineItemResponse_shouldReturnExpectedResponse() {
        LineItemEntity entity = createLineItemEntity("randomSku", 5, new BigDecimal("15.00"), "random product");

        LineItemResponse response = mapper.lineItemEntityToLineItemResponse(entity);

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(createLineItemResponse("randomSku", 5, new BigDecimal("15.00")));
    }

    @Test
    void createLineItemRequestToLineItemEntity_shouldReturnExpectedEntity() {
        CreateLineItemRequest request = new CreateLineItemRequest("randomSku", 3, new BigDecimal("12.50"));
        ProductEntity product = ProductEntity.builder().sku("randomSku").description("random product").build();

        LineItemEntity entity = mapper.createLineItemRequestToLineItemEntity(request, product);

        assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(createLineItemEntity("randomSku", 3, new BigDecimal("12.50"), "random product"));
    }

    @Test
    void shouldMapInvoiceRequestToPaymentEntity() {
        InvoiceEntity invoice = createRandomInvoiceEntity();
        PayInvoiceRequest request = new PayInvoiceRequest(PaymentMethod.STRIPE);

        PaymentEntity entity = mapper.invoiceRequestToPaymentEntity(invoice, request);
        assertThat(entity)
                .extracting(PaymentEntity::getInvoice, PaymentEntity::getAmount, PaymentEntity::getPaymentProvider, PaymentEntity::getPaymentStatus)
                .containsExactly(invoice, new BigDecimal("100.0"), PaymentProvider.STRIPE, PaymentStatus.INITIATED);
    }

    @Test
    void shouldMapPaymentEntityToPaymentResponse() {
        PaymentEntity paymentEntity = createRandomPaymentEntity(PaymentStatus.PAID);

        PaymentResponse response = mapper.paymentEntityToPaymentResponse(paymentEntity);

        assertThat(response)
                .extracting(PaymentResponse::getPaymentId, PaymentResponse::getStatus)
                .containsExactly(paymentEntity.getId().toString(), "PAID");
    }

    @Test
    void shouldCorrectlyMapPaymentMethodToProvider() {
        assertEquals(PaymentProvider.STRIPE, mapper.getPaymentProvider(PaymentMethod.STRIPE));
        assertEquals(PaymentProvider.PAYPAL, mapper.getPaymentProvider(PaymentMethod.PAYPAL));
    }
}