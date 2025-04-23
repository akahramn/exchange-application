package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import org.springframework.stereotype.Component;

@Component
public class SameCurrencyValidator implements Validator {
    @Override
    public void validate(CurrencyConversionRequest request) {
        if (request.getSourceCurrency().equals(request.getTargetCurrency())) {
            throw new IllegalArgumentException("Source and target currency must differ.");
        }
    }
}
