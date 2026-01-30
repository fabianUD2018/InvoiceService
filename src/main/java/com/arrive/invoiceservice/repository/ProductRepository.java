package com.arrive.invoiceservice.repository;

import com.arrive.invoiceservice.repository.entity.invoice.ProductEntity;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository <ProductEntity, String>{
}
