package com.arrive.invoiceservice.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenericErrorResponse (String message, Map<String, String> errors) {
    public GenericErrorResponse(String message) {
        this(message, null);
    }
}
