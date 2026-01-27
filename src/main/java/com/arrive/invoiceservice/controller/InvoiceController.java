package com.arrive.invoiceservice.controller;

import com.arrive.invoiceservice.model.request.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.response.InvoiceResponse;
import com.arrive.invoiceservice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable UUID uuid) {
        return invoiceService.getInvoice(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public InvoiceResponse createInvoice(@RequestBody @Valid CreateInvoiceRequest invoiceDto) {
        return invoiceService.createInvoice(invoiceDto);
    }

    @PatchMapping("/{uuid}/line-items")
    public ResponseEntity<InvoiceResponse> updateLineItems(@PathVariable UUID uuid, @RequestBody @Valid PatchLineItemsRequest invoiceDetails) {
        try {
            return ResponseEntity.ok(invoiceService.updateLineItems(uuid, invoiceDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
