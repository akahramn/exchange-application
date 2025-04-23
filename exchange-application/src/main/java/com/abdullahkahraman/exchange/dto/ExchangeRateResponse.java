package com.abdullahkahraman.exchange.dto;

import com.abdullahkahraman.exchange.enums.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Exchange rate response model")
public class ExchangeRateResponse {

    @Schema(description = "Source currency code", example = "USD")
    private CurrencyCode sourceCurrency;

    @Schema(description = "Target currency code", example = "EUR")
    private CurrencyCode targetCurrency;

    @Schema(description = "Latest exchange rate", example = "36.25")
    private Double rate;
}
