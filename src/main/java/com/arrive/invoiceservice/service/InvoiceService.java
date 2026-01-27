package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.mappers.InvoiceMapper;
import com.arrive.invoiceservice.model.request.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.request.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.response.InvoiceResponse;
import com.arrive.invoiceservice.repository.InvoiceRepository;
import com.arrive.invoiceservice.repository.entity.InvoiceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::invoiceEntityToInvoiceResponse)
                .toList();
    }

    public Optional<InvoiceResponse> getInvoice(UUID id) {
        return invoiceRepository.findById(id)
                .map(invoiceMapper::invoiceEntityToInvoiceResponse);
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest invoiceDto) {
        InvoiceEntity invoice = invoiceMapper.createInvoiceRequestToInvoiceEntity(invoiceDto);
        return Optional.of(invoiceRepository.save(invoice))
                .map(invoiceMapper::invoiceEntityToInvoiceResponse)
                .orElse(null);
    }

    /**
     * Used a patch operation to add line items, so i dont need to worry about managing duplicated line items information for the invoice
     * The downside of this is that everytime a client wants to update the line items, the entire set of line items is replaced,
     * which may not be ideal for large line items or frequent updates, also requires the user to query the get line items for an invoice to maintain older data
     * @param uuid
     * @param patchLineItemsRequest
     * @return
     */
    public InvoiceResponse updateLineItems(UUID uuid, PatchLineItemsRequest patchLineItemsRequest) {
        return invoiceRepository.findById(uuid)
                .map(invoice -> {
                    invoice.getLineItems().clear();
                    invoice.getLineItems().addAll(patchLineItemsRequest.getLineItems().stream().map(invoiceMapper::lineItemDtoToEntity).collect(Collectors.toSet()));
                    return invoiceRepository.save(invoice);
                })
                .map(invoiceMapper::invoiceEntityToInvoiceResponse)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id " + uuid));
    }
}
