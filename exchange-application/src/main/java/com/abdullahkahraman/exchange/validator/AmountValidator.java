package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public class AmountValidator implements Validator {
    /**
     * Validates the given currency conversion request to ensure that the amount value is greater than zero.
     *
     * @param request the currency conversion request containing the amount to validate
     * @throws IllegalArgumentException if the amount is less than or equal to zero
     * @throws NullPointerException if the amount is null
     */
    @Override
    public void validate(CurrencyConversionRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}
