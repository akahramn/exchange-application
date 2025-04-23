package com.abdullahkahraman.exchange.dto;

import com.abdullahkahraman.exchange.enums.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request model for currency conversion")
public class CurrencyConversionRequest {

    @Schema(description = "Amount to convert", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Source currency code", example = "USD")
    private CurrencyCode sourceCurrency;

    @Schema(description = "Target currency code", example = "EUR")
    private CurrencyCode targetCurrency;
}
