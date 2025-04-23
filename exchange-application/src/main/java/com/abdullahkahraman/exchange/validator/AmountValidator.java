package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public class AmountValidator implements Validator {
    @Override
    public void validate(CurrencyConversionRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}
