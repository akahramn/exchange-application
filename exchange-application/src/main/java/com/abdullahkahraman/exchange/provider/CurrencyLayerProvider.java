package com.abdullahkahraman.exchange.provider;

import com.abdullahkahraman.exchange.client.CurrencyLayerClient;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class CurrencyLayerProvider implements ExchangeRateProvider{

    private final CurrencyLayerClient client;

    /**
     * Retrieves the exchange rate between a source currency and a target currency
     * based on a given key.
     *
     * @param source the source currency code
     * @param target the target currency code
     * @param key the key used to identify the specific exchange rate within the response
     * @return the exchange rate as a Double, or throws an exception if not found
     */
    @Override
    public Double getRate(CurrencyCode source, CurrencyCode target, String key) {
        return client.fetchExchangeRate(source, target, key);
    }

    @Override
    public String getName() {
        return "CurrencyLayer";
    }
}
