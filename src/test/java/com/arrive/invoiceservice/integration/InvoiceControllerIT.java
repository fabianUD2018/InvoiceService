package com.arrive.invoiceservice.integration;

import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import com.arrive.invoiceservice.repository.InvoiceRepository;
import com.arrive.invoiceservice.repository.PaymentRepository;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private PaymentRepository paymentRepository;

    @MockitoBean
    private InvoiceRepository invoiceRepository;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getAllInvoices_shouldReturn200_whenSuccessful() throws Exception {
        when(invoiceRepository.findAll()).thenReturn(List.of(createRandomInvoiceEntity()));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/invoice").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        //todo: verify response body, normally all fields in the request body should be verified
    }

    @Test
    void getAInvoiceById_shouldReturn200_whenSuccessful() throws Exception {
        var invoice = createRandomInvoiceEntity();
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/invoice/{id}", invoice.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoice.getId().toString()));
        //todo: verify response body, normally all fields in the request body should be verified
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
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/invoice").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(createRandomInvoiceRequest())))
                .andExpect(status().isCreated());
        //todo: verify response body, normally all fields in the request body should be verified
    }

    @Test
    void updateLineItems_shouldReturn200_whenNoPayments() throws Exception {
        var invoice = createRandomInvoiceEntity();
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenAnswer(arguments -> arguments.getArgument(0));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/invoice/{id}/line-items", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(
                                createRandomPatchLineItemRequest()
                        ))
                )
                .andExpect(status().isOk());
        //todo: verify response body
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
    void payInvoice_shouldReturn200_whenPaymentSucceeds() throws Exception {
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
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.status").value("PAID"),
                        jsonPath("$.paymentId").value(paymentId.toString()));
    }

    @Test
    void payInvoice_shouldReturn200_whenPaymentHasFailedStatus() throws Exception {
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
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.status").value("FAILED"),
                        jsonPath("$.paymentId").value(paymentId.toString()));
    }

    @EnumSource(value = PaymentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"FAILED"})
    @ParameterizedTest
    void payInvoice_shouldReturn409_whenPaymentIsAlreadyPaidOrIsPending(PaymentStatus paymentStatus) throws Exception {
        var invoice = createRandomInvoiceEntity();
        var payment = createRandomPaymentEntity(paymentStatus);
        invoice.getPayments().add(payment);
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any())).thenReturn(payment);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/invoice/{id}/payment", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(
                                createPayRequest(PaymentMethod.STRIPE)
                        ))
                )
                .andExpect(status().isConflict())
                .andExpectAll(jsonPath("$.message").value("There is a payment with status: %s for invoice: %s".formatted(paymentStatus.name(), invoice.getId())),
                        jsonPath("$.errors").doesNotExist());
    }
}
