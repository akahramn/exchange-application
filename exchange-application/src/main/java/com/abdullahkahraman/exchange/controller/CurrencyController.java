package com.abdullahkahraman.exchange.controller;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.dto.CurrencyConversionResponse;
import com.abdullahkahraman.exchange.dto.ExchangeRateResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    @PostMapping("/convert")
    public ResponseEntity<List<CurrencyConversionResponse>> convert(
            @RequestPart(value = "data", required = false) CurrencyConversionRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok().body(currencyService.convertCurrency(request, file));
    }
}
