package com.arrive.invoiceservice.utils;

import com.arrive.invoiceservice.model.request.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.CreateLineItemRequest;
import com.arrive.invoiceservice.model.request.PatchLineItemsRequest;
import com.arrive.invoiceservice.repository.entity.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.LineItemEntity;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class InvoiceUtils {

    public static InvoiceEntity createRandomInvoiceEntity() {
        ArrayList<LineItemEntity> lineItems = new ArrayList<>();
        lineItems.add(LineItemEntity.builder().build());
        return InvoiceEntity.builder().id(UUID.randomUUID()).lineItems(lineItems).build();
    }

    public static CreateInvoiceRequest createRandomInvoiceRequest() {
        return CreateInvoiceRequest.builder()
                .lineItems(createRandomLineItemRequest()).build();
    }

    public static List<CreateLineItemRequest> createRandomLineItemRequest() {
        return List.of(CreateLineItemRequest.builder()
                        .price(BigDecimal.valueOf(100.0))
                        .description("Random Line Item")
                        .build());
    }

    public static PatchLineItemsRequest createRandomPatchLineItemRequest() {
        return PatchLineItemsRequest.builder().lineItems(createRandomLineItemRequest()).build();
    }
}
