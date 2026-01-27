package com.arrive.invoiceservice.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "line_item")
@Data
@NoArgsConstructor
public class LineItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdDate;



}
