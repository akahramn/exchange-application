package com.abdullahkahraman.exchange.controller;

import com.abdullahkahraman.exchange.dto.ExchangeRateResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/currency")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;

    @GetMapping("/rate")
    public ResponseEntity<ExchangeRateResponse> getExchangeRate(
            @RequestParam("sourceCurrency") CurrencyCode sourceCurrency,
            @RequestParam("targetCurrency") CurrencyCode targetCurrency
    ) {
        return ResponseEntity.ok().body(currencyService.getExchangeRate(sourceCurrency, targetCurrency));
    }


}
