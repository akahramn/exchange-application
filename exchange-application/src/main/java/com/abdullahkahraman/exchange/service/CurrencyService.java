package com.abdullahkahraman.exchange.service;

import com.abdullahkahraman.exchange.cache.CurrencyCacheService;
import com.abdullahkahraman.exchange.client.CurrencyLayerClient;
import com.abdullahkahraman.exchange.dto.*;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.CurrencyRateFetchException;
import com.abdullahkahraman.exchange.exception.TransactionNotFoundException;
import com.abdullahkahraman.exchange.model.Currency;
import com.abdullahkahraman.exchange.parser.ConversionFileParser;
import com.abdullahkahraman.exchange.parser.ConversionFileParserFactory;
import com.abdullahkahraman.exchange.repository.CurrencyRepository;
import com.abdullahkahraman.exchange.specification.ConversionTransactionSpecification;
import com.abdullahkahraman.exchange.validator.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private static final long EXCHANGE_RATE_CACHE_TTL_MINUTES = 60;

    private final CurrencyRepository currencyRepository;
    private final CurrencyCacheService currencyCacheService;
    private final CurrencyLayerClient currencyLayerClient;
    private final ConversionFileParserFactory conversionFileParserFactory;
    private final List<Validator> validators;

    /**
     * Retrieves the exchange rate between two specified currencies.
     *
     * @param sourceCurrency the source currency for the exchange rate
     * @param targetCurrency the target currency for the exchange rate
     * @return an {@code ExchangeRateResponse} containing the source currency,
     *         target currency, and the latest exchange rate value
     */
    public ExchangeRateResponse getExchangeRate(CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        Double rate = getRate(sourceCurrency, targetCurrency);
        return new ExchangeRateResponse(sourceCurrency, targetCurrency, rate);
    }

    /**
     * Retrieves the exchange rate between two specified currencies. This method first checks if the exchange rate is
     * cached; if not, it attempts to fetch the rate from an external provider and updates the cache. If both the cache
     * and the external provider fail, an exception is thrown.
     *
     * @param sourceCurrency the source currency for the exchange rate
     * @param targetCurrency the target currency for the exchange rate
     * @return the exchange rate as a {@code Double}; if a fallback value exists in the cache, it will be returned when
     *         fetching from the external provider fails
     * @throws CurrencyRateFetchException when the rate cannot be fetched from the external provider and no valid
     *         fallback exists in the cache
     */
    public Double getRate(CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        String key = sourceCurrency.toString() + targetCurrency.toString();

        if (currencyCacheService.exists(key)) {
            return currencyCacheService.getRate(key);
        }

        try {
            Double fetched = currencyLayerClient.fetchExchangeRate(sourceCurrency, targetCurrency, key);
            currencyCacheService.setRate(key, fetched, EXCHANGE_RATE_CACHE_TTL_MINUTES);
            return fetched;
        } catch (Exception e) {
            Double fallback = currencyCacheService.getRate(key);
            if (!ObjectUtils.isEmpty(fallback)) return fallback;
            throw new CurrencyRateFetchException(sourceCurrency.toString(), targetCurrency.toString(), e);
        }
    }

    /**
     * Converts currency amounts based on a single conversion request or a file containing multiple requests.
     *
     * If a {@code CurrencyConversionRequest} is provided, a single currency conversion is performed.
     * If a {@code MultipartFile} is provided, the file is parsed for multiple conversion requests.
     * The results of all conversions are returned as a list.
     *
     * @param request the request containing details for a single currency conversion, including source currency,
     *                target currency, and amount to be converted. Can be null if {@code file} is provided.
     * @param file the file containing multiple currency conversion requests. Each conversion in the file is processed
     *             and included in the result list. Can be null if {@code request} is provided.
     * @return a list of {@code CurrencyConversionResponse} objects representing the results of the currency conversions.
     *         Each response contains details such as transaction ID, source currency, target currency, source amount,
     *         and converted amount.
     */
    public List<CurrencyConversionResponse> convertCurrency(CurrencyConversionRequest request, MultipartFile file) {
        List<CurrencyConversionResponse> responseList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(request)) {
            responseList.add(convertSingleCurrency(request));
        }

        if (!ObjectUtils.isEmpty(file)) {
            responseList.addAll(convertFileCurrency(file));
        }

        return responseList;
    }

    /**
     * Converts a single currency amount from a specified source currency to a target currency.
     * The method retrieves the exchange rate, calculates the converted amount, and saves the transaction details.
     *
     * @param request the {@code CurrencyConversionRequest} containing the source currency,
     *                target currency, and the amount to be converted
     * @return a {@code CurrencyConversionResponse} containing details of the transaction,
     *         including transaction ID, source currency, target currency, source amount,
     *         and converted amount
     */
    public CurrencyConversionResponse convertSingleCurrency(CurrencyConversionRequest request) {
        validateRequest(request);

        Double rateValue = getRate(request.getSourceCurrency(), request.getTargetCurrency());
        BigDecimal result = calculateAmount(request.getAmount(), rateValue);
        String transactionId = generateTransactionId();

        saveTransaction(transactionId, request, result);
        return CurrencyConversionResponse.builder()
                .transactionId(transactionId)
                .sourceCurrency(request.getSourceCurrency())
                .targetCurrency(request.getTargetCurrency())
                .sourceAmount(request.getAmount())
                .convertedAmount(result)
                .build();
    }

    public List<CurrencyConversionResponse> convertFileCurrency( MultipartFile file) {
        List<CurrencyConversionResponse> results = new ArrayList<>();
        ConversionFileParser parser = conversionFileParserFactory.getParser(file);
        List<CurrencyConversionRequest> requests = parser.parse(file);
        for (CurrencyConversionRequest request : requests) {
            CurrencyConversionResponse response = convertSingleCurrency(request);
            results.add(response);
        }
        return results;
    }

    /**
     * Calculates the monetary amount after applying the provided rate to the given amount.
     * The result will be rounded to 4 decimal places using half-up rounding mode.
     *
     * @param amount the original monetary amount to which the rate will be applied. Must not be null.
     * @param rate the rate to apply to the amount. Must not be null.
     * @return the calculated amount as a {@code BigDecimal}, rounded to 4 decimal places.
     * @throws IllegalArgumentException if either {@code amount} or {@code rate} is null.
     */
    public static BigDecimal calculateAmount(BigDecimal amount, Double rate) {
        if (ObjectUtils.isEmpty(amount) || ObjectUtils.isEmpty(rate)) {
            throw new IllegalArgumentException("Amount and rate must not be null.");
        }
        return amount.multiply(BigDecimal.valueOf(rate)).setScale(4, RoundingMode.HALF_UP);

    }

    /**
     * Saves a currency conversion transaction in the repository.
     * The transaction includes details such as transaction ID, source currency,
     * target currency, transaction amount, and the converted result.
     *
     * @param transactionId the unique identifier for the transaction
     * @param request the {@code CurrencyConversionRequest} containing the source currency,
     *                target currency, and the amount to be converted
     * @param result the converted amount as a {@code BigDecimal}
     */
    public void saveTransaction(String transactionId, CurrencyConversionRequest request, BigDecimal result) {
        Currency transaction = Currency.builder()
                .transactionDate(LocalDateTime.now())
                .transactionId(transactionId)
                .amount(request.getAmount())
                .result(result)
                .sourceCurrency(request.getSourceCurrency())
                .targetCurrency(request.getTargetCurrency())
                .build();
        currencyRepository.save(transaction);
    }

    /**
     * Retrieves the history of currency transactions based on the provided transaction ID and date.
     *
     * @param transactionId the unique identifier of the transaction to filter by
     * @param date the specific date of the transaction to filter by
     * @param pageable the pagination and sorting information
     * @return a {@link HistoryResponse} containing the paginated and filtered transaction data
     */
    public HistoryResponse getHistory(String transactionId, LocalDate date, Pageable pageable) {
        validateTransactionIdExists(transactionId);
        Specification<Currency> spec = ConversionTransactionSpecification.filterBy(transactionId, date);
        Page<Currency> currencyPage = currencyRepository.findAll(spec, pageable);
        Page<CurrencyDto> dtoPage = currencyPage.map(this::mapCurrencyToDto);

        return new HistoryResponse(dtoPage);
    }

    /**
     * Validates whether a transaction ID exists in the repository.
     * If the transaction ID is not found, an exception is thrown.
     *
     * @param transactionId the ID of the transaction to be validated
     * @throws TransactionNotFoundException if the transaction ID does not exist in the repository
     */
    private void validateTransactionIdExists(String transactionId) {
        if (transactionId != null && !transactionId.isBlank() && !currencyRepository.existsById(transactionId)) {
            throw new TransactionNotFoundException(transactionId);
        }
    }

    /**
     * Maps a Currency object to a CurrencyDto object.
     *
     * @param currency the Currency object to be mapped
     * @return a CurrencyDto object containing the mapped data from the provided Currency object
     */
    private CurrencyDto mapCurrencyToDto(Currency currency) {
        return new CurrencyDto(
                currency.getTransactionId(),
                currency.getSourceCurrency().name(),
                currency.getTargetCurrency().name(),
                currency.getResult()
        );
    }

    /**
     * Validates the given CurrencyConversionRequest using a list of validators.
     * Each validator in the list will be invoked to perform its specific validation logic on the request.
     *
     * @param request the CurrencyConversionRequest object to be validated
     */
    private void validateRequest(CurrencyConversionRequest request) {
        for (Validator validator : validators) {
            validator.validate(request);
        }
    }

    /**
     * Generates a unique transaction identifier.
     *
     * @return A randomly generated unique transaction ID as a string.
     */
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}
