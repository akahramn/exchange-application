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
