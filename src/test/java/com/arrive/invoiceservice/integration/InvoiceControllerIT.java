package com.arrive.invoiceservice.integration;

import com.arrive.invoiceservice.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomInvoiceEntity;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomInvoiceRequest;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createRandomPatchLineItemRequest;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InvoiceControllerIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        //todo: verify response body
    }

    @Test
    void getAInvoiceById_shouldReturn200_whenSuccessful() throws Exception {
        var invoice = createRandomInvoiceEntity();
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/invoice/{id}", invoice.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoice.getId().toString()));
        //todo: verify response body
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
        //TODO: Verify response request body
    }

    @Test
    void updateLineItems_shouldReturn200_whenSuccessful() throws Exception {
        var invoice = createRandomInvoiceEntity();
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(createRandomInvoiceEntity()));
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
}
