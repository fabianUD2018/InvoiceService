package com.arrive.invoiceservice.repository;

import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends CrudRepository<PaymentEntity, UUID> {
}
