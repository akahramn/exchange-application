package com.abdullahkahraman.exchange.service;

import com.abdullahkahraman.exchange.cache.CurrencyCacheService;
import com.abdullahkahraman.exchange.client.CurrencyLayerClient;
import com.abdullahkahraman.exchange.dto.ExchangeRateResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.CurrencyRateFetchException;
import com.abdullahkahraman.exchange.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private static final long EXCHANGE_RATE_CACHE_TTL_MINUTES = 60;

    private final CurrencyRepository currencyRepository;
    private final CurrencyCacheService currencyCacheService;
    private final CurrencyLayerClient currencyLayerClient;

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
}
