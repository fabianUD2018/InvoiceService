package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.config.exceptions.InvoiceNotFoundException;
import com.arrive.invoiceservice.config.exceptions.InvoicePaymentStateException;
import com.arrive.invoiceservice.mappers.InvoiceMapperImpl;
import com.arrive.invoiceservice.model.request.lineitem.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.response.invoice.InvoiceResponse;
import com.arrive.invoiceservice.model.response.lineitem.LineItemResponse;
import com.arrive.invoiceservice.model.response.repository.ProductRepository;
import com.arrive.invoiceservice.repository.InvoiceRepository;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.invoice.ProductEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.arrive.invoiceservice.utils.InvoiceUtils.createLineItemEntity;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomInvoiceEntity;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomInvoiceRequest;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomPatchLineItemRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Spy
    private InvoiceMapperImpl invoiceMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void getAllInvoices_shouldReturnAllInvoices_whenExists() {
        InvoiceEntity invoice = createRandomInvoiceEntity();
        List<InvoiceEntity> expectedInvoices = List.of(invoice);
        when(invoiceRepository.findAll()).thenReturn(expectedInvoices);
        List<InvoiceResponse> result = invoiceService.getAllInvoices();
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(List.of(InvoiceResponse.builder()
                .id(invoice.getId())
                .lineItems(List.of(LineItemResponse.builder()
                        .sku("someSku")
                        .quantity(1)
                        .unitPrice(BigDecimal.valueOf(100.0))
                        .build()))
                .total(BigDecimal.valueOf(100.0))
                .build()));
    }

    @Test
    void getAllInvoices_shouldReturnEmptyList_whenDoNotExist() {
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());
        var result = invoiceService.getAllInvoices();
        assertThat(result).isEmpty();
    }

    @Test
    void getInvoice_shouldReturnInvoice_whenExists() {
        var id = UUID.randomUUID();
        var sku = "someSku";
        var invoice = InvoiceEntity.builder().id(id)
                .lineItems(List.of(
                                createLineItemEntity(sku, BigDecimal.ONE)
                        )
                ).build();
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        var result = invoiceService.getInvoice(id);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(InvoiceResponse
                        .builder()
                        .id(id)
                        .lineItems(List.of(
                                LineItemResponse.builder()
                                        .quantity(1)
                                        .unitPrice(BigDecimal.ONE)
                                        .sku(sku)
                                        .build()
                        ))
                        .total(BigDecimal.ONE)
                        .build()
                );
    }

    @Test
    void getInvoice_shouldThrowInvoiceNotFound_whenDoesNotExist() {
        var id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.getInvoice(id));
    }

    @Test
    void createInvoice_shouldReturnCreatedInvoice() {
        var request = createRandomInvoiceRequest();
        var id = UUID.randomUUID();
        var invoiceEntity = InvoiceEntity.builder().id(id).lineItems(List.of()).build();
        when(productRepository.findById(any(String.class))).thenReturn(Optional.of(ProductEntity.builder().sku("someSku").build()));
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenReturn(invoiceEntity);

        var result = invoiceService.createInvoice(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void updateLineItems_shouldReturnUpdatedInvoice_whenExists() {
        var request = createRandomPatchLineItemRequest();

        var existingInvoice = createRandomInvoiceEntity();
        var id = existingInvoice.getId();
        when(productRepository.findById(any(String.class))).thenReturn(Optional.of(ProductEntity.builder().sku("someSku").build()));
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.save(existingInvoice)).thenReturn(existingInvoice);

        var result = invoiceService.updateLineItems(id, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void updateLineItems_shouldInvoiceNotFoundException_whenNotFound() {
        var id = UUID.randomUUID();
        var request = createRandomPatchLineItemRequest();
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.updateLineItems(id, request));
    }

    @EnumSource(value = PaymentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"FAILED"})
    @ParameterizedTest
    void updateLineItems_shouldThrowException_whenStateIsNotCorrect(PaymentStatus paymentStatus) {
        var id = UUID.randomUUID();
        InvoiceEntity invoice = createRandomInvoiceEntity();
        PaymentEntity payment = PaymentEntity.builder().paymentStatus(paymentStatus).invoice(invoice).build();
        invoice.setPayments(List.of(payment));
        PatchLineItemsRequest request = createRandomPatchLineItemRequest();

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        assertThrows(InvoicePaymentStateException.class, () -> invoiceService.updateLineItems(id, request));
    }
}