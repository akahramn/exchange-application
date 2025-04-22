package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;

public interface Validator {
    void validate(CurrencyConversionRequest request);
}
