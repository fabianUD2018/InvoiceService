package com.arrive.invoiceservice.repository;

import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface PaymentRepository extends CrudRepository<PaymentEntity, UUID> {

    PaymentEntity findByInvoiceIdAndPaymentStatusIn(UUID invoiceId, Set<PaymentStatus> paymentStatus);
}
