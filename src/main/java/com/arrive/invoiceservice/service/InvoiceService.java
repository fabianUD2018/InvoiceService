package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.config.exceptions.InvoiceNotFoundException;
import com.arrive.invoiceservice.mappers.InvoiceMapper;
import com.arrive.invoiceservice.model.request.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.response.InvoiceResponse;
import com.arrive.invoiceservice.repository.InvoiceRepository;
import com.arrive.invoiceservice.repository.entity.InvoiceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public InvoiceResponse getInvoice(UUID uuid) {
        return invoiceMapper.invoiceEntityToInvoiceResponse(getInvoiceEntity(uuid));
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest invoiceDto) {
        InvoiceEntity invoice = invoiceMapper.createInvoiceRequestToInvoiceEntity(invoiceDto);
        return invoiceMapper.invoiceEntityToInvoiceResponse(invoiceRepository.save(invoice));
    }

    /**
     * Used a patch operation to add line items, so I don't need to worry about managing duplicated line items information for the invoice
     * The downside of this is that everytime a client wants to update the line items, the entire set of line items is replaced,
     * which may not be ideal for large line items or frequent updates, also requires the user to query the get line items endpoint for an invoice to maintain older data
     * @param uuid line item id
     * @param patchLineItemsRequest request body - contains all line items that will be created
     * @return invoice response
     */
    public InvoiceResponse updateLineItems(UUID uuid, PatchLineItemsRequest patchLineItemsRequest) {
        var invoice = getInvoiceEntity(uuid);
        invoice.getLineItems().clear();
        invoice.getLineItems().addAll(patchLineItemsRequest.getLineItems().stream().map(invoiceMapper::lineItemDtoToEntity).collect(Collectors.toSet()));
        return invoiceMapper.invoiceEntityToInvoiceResponse(invoiceRepository.save(invoice));
    }

    private InvoiceEntity getInvoiceEntity(UUID uuid) {
        return invoiceRepository.findById(uuid)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with id: " + uuid));
    }
}
