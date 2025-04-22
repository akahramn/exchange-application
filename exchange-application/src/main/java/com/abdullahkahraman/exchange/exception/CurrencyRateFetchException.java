package com.abdullahkahraman.exchange.exception;

import com.abdullahkahraman.exchange.enums.ErrorCode;

public class CurrencyRateFetchException extends BaseBusinessException{
    public CurrencyRateFetchException(String source, String target, Throwable cause) {
        super("Failed to fetch exchange rate for: " + source + " to " + target, ErrorCode.EXTERNAL_API_FAILURE);
        initCause(cause);
    }
}
