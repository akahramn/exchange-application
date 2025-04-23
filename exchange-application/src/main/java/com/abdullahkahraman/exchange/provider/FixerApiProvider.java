package com.abdullahkahraman.exchange.provider;

import com.abdullahkahraman.exchange.client.FixerApiClient;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class FixerApiProvider implements ExchangeRateProvider{
    private final FixerApiClient fixerClient;

    @Override
    public Double getRate(CurrencyCode source, CurrencyCode target, String key) {
        return fixerClient.fetchExchangeRate(source, target, key);
    }

    @Override
    public String getName() {
        return "FixerAPI";
    }
}
