package com.arrive.invoiceservice.model.response.lineitem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineItemResponse {

    private UUID id;

    private String description;

    private BigDecimal price;
}
