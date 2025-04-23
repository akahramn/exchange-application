package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SameTransactionValidatorTest {
    private SameCurrencyValidator sameCurrencyValidator;

    @BeforeEach
    void setUp() {
        sameCurrencyValidator = new SameCurrencyValidator();
    }

    @Test
    void whenTargetCurrenciesAreSame_shouldThrowExceptionWhenSourceAnd() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setSourceCurrency("USD");
        request.setTargetCurrency("USD");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sameCurrencyValidator.validate(request);
        });
        assertEquals("Source and target currency must differ.", exception.getMessage());
    }

    @Test
    void whenTargetCurrencyIsDifferentFromSource_shouldNotThrowException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setSourceCurrency("USD");
        request.setTargetCurrency("EUR");

        assertDoesNotThrow(() -> sameCurrencyValidator.validate(request));
    }

}