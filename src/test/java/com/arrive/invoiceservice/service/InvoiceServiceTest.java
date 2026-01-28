package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.config.exceptions.InvoiceNotFoundException;
import com.arrive.invoiceservice.mappers.InvoiceMapperImpl;
import com.arrive.invoiceservice.model.response.InvoiceResponse;
import com.arrive.invoiceservice.model.response.LineItemResponse;
import com.arrive.invoiceservice.repository.InvoiceRepository;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.invoice.LineItemEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private InvoiceRepository invoiceRepository;

    @Spy
    private InvoiceMapperImpl invoiceMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void getAllInvoices_shouldReturnAllInvoices_whenExists() {
        var invoice = createRandomInvoiceEntity();
        var expectedInvoices = List.of(invoice);
        when(invoiceRepository.findAll()).thenReturn(expectedInvoices);
        var result = invoiceService.getAllInvoices();
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(List.of(InvoiceResponse.builder()
                .id(invoice.getId())
                .lineItems(List.of(LineItemResponse.builder()
                        .build()))
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
        var lineItemId = UUID.randomUUID();
        var invoice = InvoiceEntity.builder().id(id).lineItems(List.of(
                LineItemEntity.builder().id(lineItemId).price(BigDecimal.ONE).build()
        )).build();
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        var result = invoiceService.getInvoice(id);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(InvoiceResponse
                    .builder()
                    .id(id)
                    .lineItems(List.of(
                            LineItemResponse.builder().price(BigDecimal.ONE)
                                    .id(lineItemId)
                                    .build()
                    ))
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
}