package com.arrive.invoiceservice.model.request.lineitem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLineItemRequest {

    @NotBlank
    @Pattern(regexp = "shoes|hat|dress", message = "SKU must be one of: shoes, hat, dress")
    private String sku;

    @NotNull
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull
    @Min(value = 1, message = "Price must be greater than 0")
    private BigDecimal unitPrice;
}
