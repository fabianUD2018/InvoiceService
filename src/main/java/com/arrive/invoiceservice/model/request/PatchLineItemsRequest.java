package com.arrive.invoiceservice.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchLineItemsRequest {

    @NotNull
    private List<@NotNull CreateLineItemRequest> lineItems;
}
