package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import org.springframework.stereotype.Component;

@Component
public class SameCurrencyValidator implements Validator {
    /**
     * Validates the given currency conversion request to ensure that the source and target currencies are different.
     *
     * @param request the currency conversion request containing the source and target currency codes
     * @throws IllegalArgumentException if the source and target currencies are the same
     */
    @Override
    public void validate(CurrencyConversionRequest request) {
        if (request.getSourceCurrency().equals(request.getTargetCurrency())) {
            throw new IllegalArgumentException("Source and target currency must differ.");
        }
    }
}
