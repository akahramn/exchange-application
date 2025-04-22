package com.abdullahkahraman.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDto {
    private String transactionId;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal convertedAmount;
}
