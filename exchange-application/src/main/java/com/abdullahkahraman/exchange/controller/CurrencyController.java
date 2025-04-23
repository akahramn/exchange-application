package com.abdullahkahraman.exchange.controller;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.dto.CurrencyConversionResponse;
import com.abdullahkahraman.exchange.dto.ExchangeRateResponse;
import com.abdullahkahraman.exchange.dto.HistoryResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.MissingSearchCriteriaException;
import com.abdullahkahraman.exchange.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/currency")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;

    @Operation(
            summary = "Get exchange rate",
            description = "Retrieves the latest exchange rate between the given source and target currencies."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rate retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid currency code provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/rate")
    public ResponseEntity<ExchangeRateResponse> getExchangeRate(
            @Parameter(
                    description = "The currency code to convert from (e.g., USD)",
                    example = "USD",
                    required = true
            )
            @RequestParam("sourceCurrency") CurrencyCode sourceCurrency,
            @Parameter(
                    description = "The currency code to convert to (e.g., EUR)",
                    example = "EUR",
                    required = true
            )
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

    @GetMapping("/history")
    public ResponseEntity<HistoryResponse> getHistory(
            @RequestParam(value = "transactionId", required = false) String transactionId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        if (ObjectUtils.isEmpty(transactionId) && ObjectUtils.isEmpty(date)) {
            throw new MissingSearchCriteriaException();
        }

        return ResponseEntity.ok(currencyService.getHistory(transactionId, date, pageable));
    }
}
