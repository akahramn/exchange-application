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

    @Override
    public Double getRate(CurrencyCode source, CurrencyCode target, String key) {
        return client.fetchExchangeRate(source, target, key);
    }

    @Override
    public String getName() {
        return "CurrencyLayer";
    }
}
