package com.abdullahkahraman.exchange.dto;

import com.abdullahkahraman.exchange.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyConversionResponse {
    private String transactionId;

    private CurrencyCode sourceCurrency;

    private CurrencyCode targetCurrency;

    private BigDecimal sourceAmount;

    private BigDecimal convertedAmount;
}
