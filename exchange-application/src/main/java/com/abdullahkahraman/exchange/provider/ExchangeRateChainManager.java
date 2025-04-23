package com.abdullahkahraman.exchange.provider;

import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.CurrencyRateFetchException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExchangeRateChainManager {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateChainManager.class);
    private final List<ExchangeRateProvider> providers;

    /**
     * Fetches the exchange rate between a source currency and a target currency
     * using a chain of {@link ExchangeRateProvider} instances. If a provider fails
     * to return a rate, the method iterates to the next provider in the chain until
     * a valid rate is obtained or all providers fail.
     *
     * @param source the source currency code
     * @param target the target currency code
     * @param key an additional key or identifier required by the providers
     * @return the exchange rate as a Double if successfully retrieved
     * @throws CurrencyRateFetchException if all providers fail to fetch the exchange rate
     */
    public Double getRate(CurrencyCode source, CurrencyCode target, String key) {
        for (ExchangeRateProvider provider : providers) {
            try {
                Double rate = provider.getRate(source, target, key);
                if (rate != null) {
                    logger.info("Exchange rate fetched successfully from {}", provider.getName());
                    return rate;
                }
            } catch (Exception e) {
                logger.warn("Provider {} failed: {}", provider.getName(), e.getMessage());
            }
        }
        throw new CurrencyRateFetchException(source.toString(), target.toString());
    }
}
