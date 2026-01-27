package com.arrive.invoiceservice.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
public class CreateLineItemRequest {

    @NotBlank
    private String description;

    @NotNull
    @Min(value = 1, message = "Price must be greater than 0")
    private BigDecimal price;
}
