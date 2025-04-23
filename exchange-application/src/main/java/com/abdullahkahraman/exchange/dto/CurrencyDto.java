package com.abdullahkahraman.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing a single currency conversion record")
public class CurrencyDto {

    @Schema(description = "Unique transaction ID", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ab")
    private String transactionId;

    @Schema(description = "Source currency code", example = "USD")
    private String sourceCurrency;

    @Schema(description = "Target currency code", example = "EUR")
    private String targetCurrency;

    @Schema(description = "Converted amount in the target currency", example = "91.24")
    private BigDecimal convertedAmount;
}
