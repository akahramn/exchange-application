package com.abdullahkahraman.exchange.dto;

import com.abdullahkahraman.exchange.enums.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response model for a single currency conversion result")
public class CurrencyConversionResponse {

    @Schema(description = "Unique ID for the conversion transaction", example = "8c28f7b1-3c71-4c2b-9c2b-12f1c12f7abc")
    private String transactionId;

    @Schema(description = "Source currency code", example = "USD")
    private CurrencyCode sourceCurrency;

    @Schema(description = "Target currency code", example = "EUR")
    private CurrencyCode targetCurrency;

    @Schema(description = "Original amount provided in source currency", example = "100.00")
    private BigDecimal sourceAmount;

    @Schema(description = "Converted amount in target currency", example = "91.24")
    private BigDecimal convertedAmount;
}
