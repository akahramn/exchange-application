package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.InvalidCurrencyException;
import org.springframework.stereotype.Component;

@Component
public class CurrencyCodeValidator implements Validator {
    /**
     * Validates the given currency conversion request by ensuring that both source and target currency codes
     * meet the required validation criteria.
     *
     * @param request the currency conversion request containing the source and target currency codes to validate
     * @throws InvalidCurrencyException if the provided source or target currency codes are invalid or unsupported
     */
    @Override
    public void validate(CurrencyConversionRequest request) {
        validateCurrency(String.valueOf(request.getSourceCurrency()), "sourceCurrency");
        validateCurrency(String.valueOf(request.getTargetCurrency()), "targetCurrency");
    }

    public void validate(String currencyCode, String fieldName) {
        validateCurrency(currencyCode, fieldName);
    }

    /**
     * Validates the provided currency code and throws an exception if the code is invalid.
     *
     * @param currencyCode the currency code to validate
     * @param fieldName the name of the field representing the currency (e.g., "sourceCurrency" or "targetCurrency")
     * @throws InvalidCurrencyException if the provided currency code is not valid or unsupported
     */
    private void validateCurrency(String currencyCode, String fieldName) {
        if (!CurrencyCode.isValid(currencyCode)) {
            throw new InvalidCurrencyException("Invalid " + fieldName + ": " + currencyCode);
        }
    }
}
