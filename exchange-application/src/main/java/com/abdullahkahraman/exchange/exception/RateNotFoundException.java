package com.abdullahkahraman.exchange.exception;

import com.abdullahkahraman.exchange.enums.ErrorCode;

public class RateNotFoundException extends BaseBusinessException{
    public RateNotFoundException(String currencyKey) {
        super("Exchange rate not found for currency pair: " + currencyKey, ErrorCode.RATE_NOT_FOUND);
    }
}
