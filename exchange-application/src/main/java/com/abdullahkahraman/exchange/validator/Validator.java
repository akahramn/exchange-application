package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.exception.InvalidCurrencyException;

public interface Validator {
    /**
     * Validates the given currency conversion request to ensure it meets the required constraints.
     *
     * @param request the currency conversion request containing details such as amount,
     *                source currency, and target currency
     * @throws IllegalArgumentException if the validation fails due to conditions like
     *                                  amount being non-positive or source and target currency being identical
     * @throws InvalidCurrencyException if the provided currency codes are invalid or unsupported
     */
    void validate(CurrencyConversionRequest request);
}
