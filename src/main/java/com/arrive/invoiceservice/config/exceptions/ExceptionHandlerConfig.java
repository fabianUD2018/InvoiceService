package com.arrive.invoiceservice.config.exceptions;

import com.arrive.invoiceservice.model.response.GenericErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public class ExceptionHandlerConfig {

    @ExceptionHandler(InvoiceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public GenericErrorResponse handleException(InvoiceNotFoundException invoiceNotFoundException) {
        log.debug("error while searching for invoice", invoiceNotFoundException);
        return new GenericErrorResponse("Invoice not found on database");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GenericErrorResponse handleException(MethodArgumentNotValidException validationException) {
        log.debug("Error validating request parameters", validationException);
        return new GenericErrorResponse("Invalid request parameters.",
                validationException
                        .getFieldErrors()
                        .stream()
                .collect(Collectors.toMap(ObjectError::getObjectName,
                        e -> Optional.ofNullable(e.getDefaultMessage()).orElse("Invalid field"))));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericErrorResponse handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return new GenericErrorResponse(exception.getMessage());
    }

}
