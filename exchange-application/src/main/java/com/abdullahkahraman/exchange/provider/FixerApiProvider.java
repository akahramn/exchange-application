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

    /**
     * Retrieves the exchange rate between a source currency and a target currency
     * using the specified key for identification or context.
     *
     * @param source the source currency code from which the exchange rate calculation starts
     * @param target the target currency code to which the exchange rate calculation is performed
     * @param key an additional key or identifier required to fetch the specific exchange rate
     * @return the exchange rate as a Double, or null if the rate is not found or cannot be retrieved
     */
    @Override
    public Double getRate(CurrencyCode source, CurrencyCode target, String key) {
        return fixerClient.fetchExchangeRate(source, target, key);
    }

    @Override
    public String getName() {
        return "FixerAPI";
    }
}
