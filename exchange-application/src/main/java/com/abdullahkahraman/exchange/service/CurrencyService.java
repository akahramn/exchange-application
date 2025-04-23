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

import java.io.IOException;
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

    public ExchangeRateResponse getExchangeRate(CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        Double rate = getRate(sourceCurrency, targetCurrency);
        return new ExchangeRateResponse(sourceCurrency, targetCurrency, rate);
    }

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

    public static BigDecimal calculateAmount(BigDecimal amount, Double rate) {
        if (ObjectUtils.isEmpty(amount) || ObjectUtils.isEmpty(rate)) {
            throw new IllegalArgumentException("Amount and rate must not be null.");
        }
        return amount.multiply(BigDecimal.valueOf(rate)).setScale(4, RoundingMode.HALF_UP);

    }

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

    public HistoryResponse getHistory(String transactionId, LocalDate date, Pageable pageable) {
        Specification<Currency> spec =
                ConversionTransactionSpecification.filterBy(transactionId, date);

        if (!ObjectUtils.isEmpty(transactionId) && !currencyRepository.existsById(transactionId)) {
            throw new TransactionNotFoundException(transactionId);
        }

        Page<Currency> currencyPage = currencyRepository.findAll(spec, pageable);

        Page<CurrencyDto> dtoPage = currencyPage.map(currency -> new CurrencyDto(
                currency.getTransactionId(),
                currency.getSourceCurrency().name(),
                currency.getTargetCurrency().name(),
                currency.getResult()
        ));

        return new HistoryResponse(dtoPage);
    }

    private void validateRequest(CurrencyConversionRequest request) {
        for (Validator validator : validators) {
            validator.validate(request);
        }
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}
