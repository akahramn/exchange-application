package com.abdullahkahraman.exchange.model;

import com.abdullahkahraman.exchange.enums.CurrencyCode;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    private String transactionId;

    private CurrencyCode sourceCurrency;
    private CurrencyCode targetCurrency;
    private BigDecimal amount;
    private BigDecimal result;
    private LocalDateTime transactionDate;
}
