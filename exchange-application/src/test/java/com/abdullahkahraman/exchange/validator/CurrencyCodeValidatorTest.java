package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.InvalidCurrencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyCodeValidatorTest {

    private CurrencyCodeValidator currencyCodeValidator;
    @BeforeEach
    void setUp() {
        currencyCodeValidator = new CurrencyCodeValidator();
    }

    @Test
    void whenValidateValidCurrencyCodes_shouldNotThrowException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setSourceCurrency("USD");
        request.setTargetCurrency("EUR");

        assertDoesNotThrow(() -> currencyCodeValidator.validate(request));
    }

    @Test
    void whenSourceCurrencyIsInvalid_shouldThrowInvalidCurrencyException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setSourceCurrency(null); // Simulate invalid source currency
        request.setTargetCurrency("EUR");

        InvalidCurrencyException exception = assertThrows(
                InvalidCurrencyException.class,
                () -> currencyCodeValidator.validate(request)
        );

        assertTrue(exception.getMessage().contains("Invalid sourceCurrency"));
    }

    @Test
    void whenTargetCurrencyIsInvalid_shouldThrowInvalidCurrencyException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setSourceCurrency("USD");
        request.setTargetCurrency(null); // Simulate invalid target currency

        InvalidCurrencyException exception = assertThrows(
                InvalidCurrencyException.class,
                () -> currencyCodeValidator.validate(request)
        );

        assertTrue(exception.getMessage().contains("Invalid targetCurrency"));
    }

}