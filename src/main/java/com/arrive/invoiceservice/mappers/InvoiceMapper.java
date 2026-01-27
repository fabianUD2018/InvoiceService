package com.arrive.invoiceservice.mappers;

import com.arrive.invoiceservice.model.request.CreateInvoiceRequest;
import com.arrive.invoiceservice.model.request.CreateLineItemRequest;
import com.arrive.invoiceservice.model.response.InvoiceResponse;
import com.arrive.invoiceservice.repository.entity.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.LineItemEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
/*
    Mapstructs mapper
    This class should have unit test even though is an interface;
    Given time constraints, I won't spend time on this.
 */
public interface InvoiceMapper {

    InvoiceResponse invoiceEntityToInvoiceResponse(InvoiceEntity invoiceEntity);

    InvoiceEntity createInvoiceRequestToInvoiceEntity(CreateInvoiceRequest invoiceDto);

    LineItemEntity lineItemDtoToEntity(CreateLineItemRequest createLineItemRequest);
}
