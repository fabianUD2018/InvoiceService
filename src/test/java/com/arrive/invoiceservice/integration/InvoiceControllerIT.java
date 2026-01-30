package com.arrive.invoiceservice.integration;

import com.arrive.invoiceservice.model.request.invoice.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.lineitem.PatchLineItemsRequest;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import com.arrive.invoiceservice.model.response.repository.ProductRepository;
import com.arrive.invoiceservice.repository.InvoiceRepository;
import com.arrive.invoiceservice.repository.PaymentRepository;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.invoice.ProductEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.arrive.invoiceservice.utils.InvoiceUtils.createInvoiceRequest;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createPatchLineItemRequest;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createPayRequest;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomInvoiceEntity;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomInvoiceRequest;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomPatchLineItemRequest;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomPaymentEntity;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class InvoiceControllerIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @MockitoBean
    private InvoiceRepository invoiceRepository;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getAllInvoices_shouldReturn200_whenSuccessful() throws Exception {
        var invoice = createRandomInvoiceEntity();
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/invoice").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(invoice.getId().toString()))
                .andExpect(jsonPath("$[0].total").value(100.0))
                .andExpect(jsonPath("$[0].lineItems", hasSize(1)))
                .andExpect(jsonPath("$[0].lineItems[0].sku").value("shoes"))
                .andExpect(jsonPath("$[0].lineItems[0].quantity").value(1))
                .andExpect(jsonPath("$[0].lineItems[0].unitPrice").value(100.0));
    }

    @Test
    void getAInvoiceById_shouldReturn200_whenSuccessful() throws Exception {
        var invoice = createRandomInvoiceEntity();
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/invoice/{id}", invoice.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoice.getId().toString()))
                .andExpect(jsonPath("$.total").value(100.0))
                .andExpect(jsonPath("$.lineItems", hasSize(1)))
                .andExpect(jsonPath("$.lineItems[0].sku").value("shoes"))
                .andExpect(jsonPath("$.lineItems[0].quantity").value(1))
                .andExpect(jsonPath("$.lineItems[0].unitPrice").value(100.0));
    }

    @Test
    void getAInvoiceById_shouldReturn404_whenNotFound() throws Exception {
        when(invoiceRepository.findById(any())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/invoice/{id}", UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invoice not found on database"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void createInvoice_shouldReturn201_whenSuccessful() throws Exception {
        var request = createRandomInvoiceRequest();
        var sku = request.getLineItems().get(0).getSku();
        var quantity = request.getLineItems().get(0).getQuantity();
        var price = request.getLineItems().get(0).getUnitPrice();

        when(productRepository.findById(sku)).thenReturn(Optional.of(ProductEntity.builder().sku(sku).description("Desc").build()));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> {
            InvoiceEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/invoice").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.total").value(price.multiply(BigDecimal.valueOf(quantity)).doubleValue()))
                .andExpect(jsonPath("$.lineItems", hasSize(1)))
                .andExpect(jsonPath("$.lineItems[0].sku").value(sku))
                .andExpect(jsonPath("$.lineItems[0].quantity").value(quantity))
                .andExpect(jsonPath("$.lineItems[0].unitPrice").value(price.doubleValue()));
    }

    @Test
    void updateLineItems_shouldReturn200_whenNoPayments() throws Exception {
        var invoice = createRandomInvoiceEntity();
        var patchRequest = createRandomPatchLineItemRequest();
        var newSku = patchRequest.getLineItems().get(0).getSku();
        var newQuantity = patchRequest.getLineItems().get(0).getQuantity();
        var newPrice = patchRequest.getLineItems().get(0).getUnitPrice();

        when(productRepository.findById(any())).thenReturn(Optional.of(ProductEntity.builder().sku(newSku).description("New Desc").build()));
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenAnswer(arguments -> arguments.getArgument(0));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/invoice/{id}/line-items", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(patchRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoice.getId().toString()))
                .andExpect(jsonPath("$.total").value(newPrice.multiply(BigDecimal.valueOf(newQuantity)).doubleValue()))
                .andExpect(jsonPath("$.lineItems", hasSize(1)))
                .andExpect(jsonPath("$.lineItems[0].sku").value(newSku))
                .andExpect(jsonPath("$.lineItems[0].quantity").value(newQuantity))
                .andExpect(jsonPath("$.lineItems[0].unitPrice").value(newPrice.doubleValue()));
    }

    @EnumSource(value = PaymentStatus.class, mode = EnumSource.Mode.INCLUDE, names = {"PAID", "PENDING"})
    @ParameterizedTest
    void updateLineItems_shouldReturn409_whenAlreadyPaidOrWaitingConfirmation(PaymentStatus paymentStatus) throws Exception {
        InvoiceEntity invoice = createRandomInvoiceEntity();
        var payment = PaymentEntity.builder().id(UUID.randomUUID()).paymentStatus(paymentStatus).build();
        invoice.getPayments().add(payment);
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenAnswer(arguments -> arguments.getArgument(0));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/invoice/{id}/line-items", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(
                                createRandomPatchLineItemRequest()
                        ))
                )
                .andExpect(status().isConflict())
                .andExpectAll(jsonPath("$.message")
                                .value("There is a payment with status: %s for invoice: %s".formatted(paymentStatus.name(), invoice.getId())),
                        jsonPath("$.errors").doesNotExist());
    }

    @Test
    void payInvoice_shouldReturn201_whenPaymentSucceeds() throws Exception {
        var invoice = createRandomInvoiceEntity();
        var paymentId = UUID.randomUUID();
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any())).thenAnswer(arguments -> {
            PaymentEntity payment = arguments.getArgument(0);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setId(paymentId);
            return payment;
        });

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/invoice/{id}/payment", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(
                                createPayRequest(PaymentMethod.PAYPAL)
                        ))
                )
                .andExpect(status().isCreated())
                .andExpectAll(jsonPath("$.status").value("PAID"),
                        jsonPath("$.paymentId").value(paymentId.toString()));
    }

    @Test
    void payInvoice_shouldReturn201_whenPaymentHasFailedStatus() throws Exception {
        var invoice = createRandomInvoiceEntity();
        var paymentId = UUID.randomUUID();
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any())).thenAnswer(arguments -> {
            PaymentEntity payment = arguments.getArgument(0);
            payment.setId(paymentId);
            return payment;
        });

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/invoice/{id}/payment", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(
                                createPayRequest(PaymentMethod.STRIPE)
                        ))
                )
                .andExpect(status().isCreated())
                .andExpectAll(jsonPath("$.status").value("FAILED"),
                        jsonPath("$.paymentId").value(paymentId.toString()));
    }

    @EnumSource(value = PaymentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"FAILED"})
    @ParameterizedTest
    void payInvoice_shouldReturn409_whenThereIsAValidPaymentInDb(PaymentStatus paymentStatus) throws Exception {
        var invoice = createRandomInvoiceEntity();
        var payment = createRandomPaymentEntity(paymentStatus);
        invoice.getPayments().add(payment);
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        when(paymentRepository.findByInvoiceIdAndPaymentStatusIn(any(), any())).thenReturn(payment);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/invoice/{id}/payment", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(
                                createPayRequest(PaymentMethod.STRIPE)
                        ))
                )
                .andExpect(status().isConflict())
                .andExpectAll(jsonPath("$.message").value("Payment is in %s for invoice: %s".formatted(paymentStatus.name(), invoice.getId())),
                        jsonPath("$.errors").doesNotExist());
    }
    
    
    @ParameterizedTest
    @MethodSource("createErroredInvoiceRequestMethod")
    void createInvoice_shouldReturnError_whenRequestIsInvalid(CreateInvoiceRequest request, HttpStatus expectedStatus) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()));
    }

    @ParameterizedTest
    @MethodSource("createErroredPatchLineItemsRequestMethod")
    void updateLineItems_shouldReturnError_whenRequestIsInvalid(PatchLineItemsRequest request, HttpStatus expectedStatus) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/invoice/{id}/line-items", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()));
    }

    @ParameterizedTest
    @MethodSource("createErroredPayInvoiceRequestMethod")
    void payInvoice_shouldReturnError_whenRequestIsInvalid(PayInvoiceRequest request, HttpStatus expectedStatus) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/invoice/{id}/payment", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()));
    }

    private static Stream<Arguments> createErroredInvoiceRequestMethod() {
        return Stream.of(
                Arguments.of(createInvoiceRequest(null, 1, BigDecimal.valueOf(100)), HttpStatus.BAD_REQUEST),
                Arguments.of(createInvoiceRequest("invalid", 1, BigDecimal.valueOf(100)), HttpStatus.BAD_REQUEST),
                Arguments.of(createInvoiceRequest("shoes", 0, BigDecimal.valueOf(100)), HttpStatus.BAD_REQUEST),
                Arguments.of(createInvoiceRequest("shoes", 1, BigDecimal.ZERO), HttpStatus.BAD_REQUEST),
                Arguments.of(CreateInvoiceRequest.builder().lineItems(null).build(), HttpStatus.BAD_REQUEST)
        );
    }

    private static Stream<Arguments> createErroredPatchLineItemsRequestMethod() {
        return Stream.of(
                Arguments.of(createPatchLineItemRequest(null, 1, BigDecimal.valueOf(100)), HttpStatus.BAD_REQUEST),
                Arguments.of(createPatchLineItemRequest("invalid", 1, BigDecimal.valueOf(100)), HttpStatus.BAD_REQUEST),
                Arguments.of(createPatchLineItemRequest("shoes", 0, BigDecimal.valueOf(100)), HttpStatus.BAD_REQUEST),
                Arguments.of(createPatchLineItemRequest("shoes", 1, BigDecimal.ZERO), HttpStatus.BAD_REQUEST),
                Arguments.of(PatchLineItemsRequest.builder().lineItems(null).build(), HttpStatus.BAD_REQUEST)
        );
    }

    private static Stream<Arguments> createErroredPayInvoiceRequestMethod() {
        return Stream.of(
                Arguments.of(PayInvoiceRequest.builder().paymentMethod(null).build(), HttpStatus.BAD_REQUEST)
        );
    }
    
}
