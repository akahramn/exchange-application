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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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

    @Operation(
            summary = "Convert currency via JSON or file upload",
            description = "Converts an amount from source currency to target currency. You can either send a JSON payload or upload a CSV or EXCEL file containing multiple conversion requests."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversion completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/convert")
    public ResponseEntity<List<CurrencyConversionResponse>> convert(
            @Parameter(
                    description = "JSON data for a single currency conversion request",
                    required = false,
                    schema = @Schema(implementation = CurrencyConversionRequest.class)
            )
            @RequestPart(value = "data", required = false) CurrencyConversionRequest request,

            @Parameter(
                    description = "CSV or EXCEL file for bulk currency conversions",
                    required = false,
                    content = @Content(mediaType = "multipart/form-data")
            )
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok().body(currencyService.convertCurrency(request, file));
    }

    @Operation(
            summary = "Get conversion history",
            description = "Returns a paginated list of past currency conversion transactions. At least one of 'transactionId' or 'date' must be provided."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing both transactionId and date"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/history")
    public ResponseEntity<HistoryResponse> getHistory(
            @Parameter(description = "Transaction ID to filter history", example = "a1b2c3d4-e5f6")
            @RequestParam(value = "transactionId", required = false) String transactionId,

            @Parameter(description = "Transaction date to filter history", example = "2024-12-15")
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable
    ) {
        validateSearchCriteria(transactionId, date);
        HistoryResponse response = currencyService.getHistory(transactionId, date, pageable);
        return ResponseEntity.ok(response);
    }


    private void validateSearchCriteria(String transactionId, LocalDate date) {
        boolean isTransactionIdMissing = transactionId == null || transactionId.isBlank();
        boolean isDateMissing = date == null;

        if (isTransactionIdMissing && isDateMissing) {
            throw new MissingSearchCriteriaException();
        }
    }
}
