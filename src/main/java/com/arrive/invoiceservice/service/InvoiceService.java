package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.model.exceptions.InvoiceNotFoundException;
import com.arrive.invoiceservice.model.exceptions.InvoicePaymentStateException;
import com.arrive.invoiceservice.mappers.InvoiceMapper;
import com.arrive.invoiceservice.model.exceptions.ProductNotFoundException;
import com.arrive.invoiceservice.model.request.invoice.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.lineitem.CreateLineItemRequest;
import com.arrive.invoiceservice.model.request.lineitem.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.response.invoice.InvoiceResponse;
import com.arrive.invoiceservice.repository.ProductRepository;
import com.arrive.invoiceservice.repository.InvoiceRepository;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.invoice.LineItemEntity;
import com.arrive.invoiceservice.repository.entity.invoice.ProductEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
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
        List<LineItemEntity> lineItemEntity = invoiceDto.getLineItems().stream()
                .map(this::getLineItemEntity)
                .toList();
        InvoiceEntity invoice = InvoiceEntity.builder().lineItems(lineItemEntity).build();
        return invoiceMapper.invoiceEntityToInvoiceResponse(invoiceRepository.save(invoice));
    }

    /**
     * Used a patch operation to add line items, so I don't need to worry about managing duplicated line items information for the invoice
     * The downside of this is that everytime a client wants to update the line items, the entire set of line items is replaced,
     * which may not be ideal for large line items or frequent updates, also requires the user to query the get line items endpoint for an invoice to maintain older data
     *
     * @param uuid                  line item id
     * @param patchLineItemsRequest request body - contains all line items that will be created
     * @return invoice response
     */
    public InvoiceResponse updateLineItems(UUID uuid, PatchLineItemsRequest patchLineItemsRequest) {
        var invoice = getInvoiceEntity(uuid);
        verifyInvoiceState(invoice);
        invoice.getLineItems().clear();
        invoice.getLineItems().addAll(
                patchLineItemsRequest.getLineItems()
                .stream()
                .map(this::getLineItemEntity).toList()
        );
        return invoiceMapper.invoiceEntityToInvoiceResponse(invoiceRepository.save(invoice));
    }

    public InvoiceEntity getInvoiceEntity(UUID uuid) {
        return invoiceRepository.findById(uuid)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with id: " + uuid));
    }

    /**
     * Verifies that the invoice is in a state that allows for updates, specifically that there are no pending or paid payments
     *
     * @param invoice invoice entity to verify
     */
    public void verifyInvoiceState(InvoiceEntity invoice) {
        invoice.getPayments().stream()
                .filter(payment ->
                        List.of(PaymentStatus.INITIATED, PaymentStatus.PAID, PaymentStatus.PENDING).contains(payment.getPaymentStatus()))
                .findFirst()
                .ifPresent(payment -> {
                    throw new InvoicePaymentStateException("There is a payment with status: " + payment.getPaymentStatus() + " for invoice: " + invoice.getId());
                });
    }

    private LineItemEntity getLineItemEntity(CreateLineItemRequest createLineItemRequest) {
        ProductEntity product = productRepository.findById(createLineItemRequest.getSku())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found: sku %s".formatted(createLineItemRequest.getSku())
                ));

        return invoiceMapper.createLineItemRequestToLineItemEntity(createLineItemRequest, product);
    }
}
