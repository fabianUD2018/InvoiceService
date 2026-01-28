package com.arrive.invoiceservice.model.request.lineitem;

import jakarta.validation.Valid;
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
    private List<@Valid @NotNull CreateLineItemRequest> lineItems;
}
