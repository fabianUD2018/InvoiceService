package com.arrive.invoiceservice.model.response.invoice;

import com.arrive.invoiceservice.model.response.lineitem.LineItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private UUID id;
    private Instant createdDate;
    private List<LineItemResponse> lineItems;
    private BigDecimal total;
}
