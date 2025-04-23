package com.abdullahkahraman.exchange.provider;

import com.abdullahkahraman.exchange.enums.CurrencyCode;

public interface ExchangeRateProvider {
    Double getRate(CurrencyCode source, CurrencyCode target, String key);
    String getName();
}
