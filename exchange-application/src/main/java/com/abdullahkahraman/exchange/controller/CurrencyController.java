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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);

    private final CurrencyService currencyService;

    /**
     * Retrieves the latest exchange rate between the given source and target currencies.
     *
     * @param sourceCurrency the currency code to convert from (e.g., USD)
     * @param targetCurrency the currency code to convert to (e.g., EUR)
     * @return a {@link ResponseEntity} containing an {@link ExchangeRateResponse} object with the exchange rate information
     */
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
        logger.info("Received request to get exchange rate: {} -> {}", sourceCurrency, targetCurrency);

        ExchangeRateResponse response = currencyService.getExchangeRate(sourceCurrency, targetCurrency);

        logger.debug("Exchange rate response: {}", response);

        return ResponseEntity.ok().body(response);
    }

    /**
     * Handles the currency conversion process, allowing input through a JSON payload for a single conversion
     * request or a CSV/Excel file for multiple conversion requests. Converts the amount from the source currency
     * to the target currency and returns the result.
     *
     * @param request JSON data representing a single currency conversion request (optional).
     * @param file CSV or Excel file containing multiple currency conversion requests (optional).
     * @return a {@link ResponseEntity} encapsulating a list of {@link CurrencyConversionResponse} objects with
     *         the conversion results.
     */
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
        if (!ObjectUtils.isEmpty(request)) {
            logger.info("Received single currency conversion request: {} -> {}, amount={}",
                    request.getSourceCurrency(), request.getTargetCurrency(), request.getAmount());
        }

        if (!ObjectUtils.isEmpty(file)) {
            logger.info("Received bulk currency conversion file upload: filename={}, size={} bytes",
                    file.getOriginalFilename(), file.getSize());
        }

        List<CurrencyConversionResponse> result = currencyService.convertCurrency(request, file);

        logger.debug("Currency conversion result: {}", result);

        return ResponseEntity.ok().body(result);
    }

    /**
     * Retrieves a paginated list of past currency conversion transactions.
     * At least one of 'transactionId' or 'date' must be provided as a search criterion.
     *
     * @param transactionId the ID of a specific transaction to filter the history (optional)
     * @param date the date of transactions to filter the history (optional)
     * @param pageable pagination details such as page number and size
     * @return a {@link ResponseEntity} containing a {@link HistoryResponse} object with the filtered transaction history
     */
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
        logger.info("Received history request: transactionId='{}', date='{}', page={}, size={}",
                transactionId, date, pageable.getPageNumber(), pageable.getPageSize());

        validateSearchCriteria(transactionId, date);

        HistoryResponse response = currencyService.getHistory(transactionId, date, pageable);

        logger.debug("Returning {} records in history response", response.getContent().size());

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
