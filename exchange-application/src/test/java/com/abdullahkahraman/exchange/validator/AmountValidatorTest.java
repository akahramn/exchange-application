package com.abdullahkahraman.exchange.validator;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AmountValidatorTest {
    private AmountValidator amountValidator;

    @BeforeEach
    void setUp() {
        amountValidator = new AmountValidator();
    }

    @Test
    public void whenValidateWithPositiveAmount_shouldNotThrowException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(BigDecimal.valueOf(100.00));

        assertDoesNotThrow(() -> amountValidator.validate(request));
    }

    @Test
    public void whenValidateWithNegativeAmount_shouldThrowException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(BigDecimal.valueOf(-50.00));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> amountValidator.validate(request));

        assertEquals("Amount must be greater than zero.", exception.getMessage());
    }

    @Test
    public void whenValidateWithZeroAmount_shouldThrowException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(BigDecimal.ZERO);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> amountValidator.validate(request));

        assertEquals("Amount must be greater than zero.", exception.getMessage());
    }

    @Test
    public void whenValidateWithNullAmount_shouldThrowException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(null);

        assertThrows(NullPointerException.class, () -> amountValidator.validate(request));
    }

}