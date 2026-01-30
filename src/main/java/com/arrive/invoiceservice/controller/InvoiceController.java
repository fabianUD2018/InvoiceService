package com.arrive.invoiceservice.controller;

import com.arrive.invoiceservice.model.request.invoice.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.lineitem.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.response.GenericErrorResponse;
import com.arrive.invoiceservice.model.response.invoice.InvoiceResponse;
import com.arrive.invoiceservice.model.response.payment.PaymentResponse;
import com.arrive.invoiceservice.service.InvoiceService;
import com.arrive.invoiceservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Invoice", description = "Endpoints for managing invoices and payments")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all invoices", description = "Retrieves a list of all invoices in the system")
    @ApiResponse(responseCode = "200", description = "List of invoices retrieved successfully")
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get invoice by UUID", description = "Retrieves a single invoice by its unique identifier")
    @ApiResponse(responseCode = "200", description = "Invoice found and returned")
    @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(schema = @Schema(implementation = GenericErrorResponse.class)))
    public InvoiceResponse getInvoiceById(@PathVariable UUID uuid) {
        return invoiceService.getInvoice(uuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new invoice", description = "Creates a new invoice with the provided details")
    @ApiResponse(responseCode = "201", description = "Invoice created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema(implementation = GenericErrorResponse.class)))
    public InvoiceResponse createInvoice(
            @RequestBody @Valid CreateInvoiceRequest invoiceDto) {
        return invoiceService.createInvoice(invoiceDto);
    }

    @PatchMapping("/{uuid}/line-items")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update invoice line items", description = "Updates the line items of an existing invoice. Cannot be updated if invoice is already paid.")
    @ApiResponse(responseCode = "200", description = "Line items updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema(implementation = GenericErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(schema = @Schema(implementation = GenericErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Invoice cannot be updated (e.g., already paid)", content = @Content(schema = @Schema(implementation = GenericErrorResponse.class)))
    public InvoiceResponse updateLineItems(
            @PathVariable UUID uuid,
            @RequestBody @Valid PatchLineItemsRequest invoiceDetails) {
        return invoiceService.updateLineItems(uuid, invoiceDetails);
    }

    @PostMapping("/{uuid}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pay an invoice", description = "Processes a payment for the specified invoice")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payment details", content = @Content(schema = @Schema(implementation = GenericErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(schema = @Schema(implementation = GenericErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Invoice already paid or in an invalid state for payment", content = @Content(schema = @Schema(implementation = GenericErrorResponse.class)))
    public PaymentResponse payInvoice(@PathVariable UUID uuid, @RequestBody @Valid PayInvoiceRequest payInvoiceRequest) {
        return paymentService.processPayment(uuid, payInvoiceRequest);
    }

}
