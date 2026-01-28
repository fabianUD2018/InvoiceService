package com.arrive.invoiceservice.config.exceptions;

import com.arrive.invoiceservice.model.response.GenericErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionHandlerConfigTest {

    private final ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

    @Test
    void handleException_InvoiceNotFoundException_shouldReturnNotFound() {
        InvoiceNotFoundException exception = new InvoiceNotFoundException("Not found");
        GenericErrorResponse response = exceptionHandlerConfig.handleException(exception);
        assertThat(response.message()).isEqualTo(("Invoice not found on database"));
    }

    @Test
    void handleException_InvoicePaymentStateException_shouldReturnConflict() {
        InvoicePaymentStateException exception = new InvoicePaymentStateException("Invalid invoice payment state");
        GenericErrorResponse response = exceptionHandlerConfig.handleException(exception);

        assertThat(response.message()).isEqualTo(("Invalid invoice payment state"));
    }

    @Test
    void handleException_MethodArgumentNotValidException_shouldReturnBadRequest() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        FieldError fieldError1 = new FieldError("someDto", "fieldOne", "messageOne");
        FieldError fieldError2 = new FieldError("someDto", "fieldTwo", "messageTwo");

        when(exception.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        GenericErrorResponse response = exceptionHandlerConfig.handleException(exception);

        assertThat(response).usingRecursiveComparison().isEqualTo(new GenericErrorResponse("Invalid request parameters.",
                Map.of("fieldOne", "messageOne", "fieldTwo", "messageTwo")));
    }

    @Test
    void handleException_HttpMessageNotReadableException_shouldReturnBadRequest() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Error reading message", null);
        GenericErrorResponse response = exceptionHandlerConfig.handleException(exception);

        assertThat(response).usingRecursiveComparison().isEqualTo(new GenericErrorResponse("Error reading message"));
    }

    @Test
    void handleException_GenericException_shouldReturnInternalServerError() {
        Exception exception = new Exception("Unknown error message");
        GenericErrorResponse response = exceptionHandlerConfig.handleException(exception);

        assertThat(response).usingRecursiveComparison().isEqualTo(new GenericErrorResponse("Internal server error"));
    }
}