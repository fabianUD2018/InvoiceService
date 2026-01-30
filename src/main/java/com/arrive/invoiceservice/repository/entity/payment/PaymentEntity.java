package com.arrive.invoiceservice.repository.entity.payment;

import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    private Instant createdDate;

    private Instant paidDate;

    @Enumerated
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private PaymentProvider paymentProvider;

    @Enumerated
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private PaymentStatus paymentStatus;

    private BigDecimal amount;

    @ManyToOne
    private InvoiceEntity invoice;
}
