package com.abdullahkahraman.exchange.exception;

import com.abdullahkahraman.exchange.enums.ErrorCode;

public class InvalidCurrencyException extends BaseBusinessException{
    public InvalidCurrencyException(String currencyCode) {
        super("Invalid currency code: " + currencyCode, ErrorCode.INVALID_CURRENCY);
    }
}
