package com.arrive.invoiceservice.controller;

import com.arrive.invoiceservice.model.request.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.response.InvoiceResponse;
import com.arrive.invoiceservice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public InvoiceResponse getInvoiceById(@PathVariable UUID uuid) {
        return invoiceService.getInvoice(uuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@RequestBody @Valid CreateInvoiceRequest invoiceDto) {
        return invoiceService.createInvoice(invoiceDto);
    }

    @PatchMapping("/{uuid}/line-items")
    @ResponseStatus(HttpStatus.OK)
    public InvoiceResponse updateLineItems(@PathVariable UUID uuid, @RequestBody @Valid PatchLineItemsRequest invoiceDetails) {
        return invoiceService.updateLineItems(uuid, invoiceDetails);
    }

}
