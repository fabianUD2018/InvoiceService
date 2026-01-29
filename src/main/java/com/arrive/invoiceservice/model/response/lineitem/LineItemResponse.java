package com.arrive.invoiceservice.model.response.lineitem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineItemResponse {

    private String sku;
    private int quantity;
    private BigDecimal unitPrice;
}
