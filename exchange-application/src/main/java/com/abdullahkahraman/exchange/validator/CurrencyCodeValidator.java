package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.InvalidCurrencyException;

public class CurrencyCodeValidator implements Validator {
    @Override
    public void validate(CurrencyConversionRequest request) {
        validateCurrency(String.valueOf(request.getSourceCurrency()), "sourceCurrency");
        validateCurrency(String.valueOf(request.getTargetCurrency()), "targetCurrency");
    }

    private void validateCurrency(String currencyCode, String fieldName) {
        if (!CurrencyCode.isValid(currencyCode)) {
            throw new InvalidCurrencyException("Invalid " + fieldName + ": " + currencyCode);
        }
    }
}
