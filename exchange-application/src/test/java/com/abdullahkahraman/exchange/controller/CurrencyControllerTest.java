package com.abdullahkahraman.exchange.controller;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.dto.CurrencyConversionResponse;
import com.abdullahkahraman.exchange.dto.ExchangeRateResponse;
import com.abdullahkahraman.exchange.dto.HistoryResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.MissingSearchCriteriaException;
import com.abdullahkahraman.exchange.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyControllerTest {

    private CurrencyService currencyService;
    private CurrencyController currencyController;

    @BeforeEach
    void setUp() {
        currencyService = Mockito.mock(CurrencyService.class);
        currencyController = new CurrencyController(currencyService);
    }

    @Test
    public void whenConvertCurrencyWithValidRequest_shouldReturnSuccessResponse() throws Exception {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        List<CurrencyConversionResponse> mockResponse = List.of(new CurrencyConversionResponse());

        Mockito.when(currencyService.convertCurrency(Mockito.eq(request), Mockito.isNull()))
                .thenReturn(mockResponse);

        ResponseEntity<List<CurrencyConversionResponse>> response = currencyController.convert(request, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenConvertCurrencyWithFile_shouldReturnSuccessResponse() throws Exception {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        List<CurrencyConversionResponse> mockResponse = List.of(new CurrencyConversionResponse());

        Mockito.when(currencyService.convertCurrency(Mockito.isNull(), Mockito.eq(mockFile)))
                .thenReturn(mockResponse);

        ResponseEntity<List<CurrencyConversionResponse>> response = currencyController.convert(null, mockFile);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenConvertCurrencyThrowsException_shouldHandleErrorGracefully() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        IOException exception = new IOException("Test exception");

        try {
            Mockito.when(currencyService.convertCurrency(Mockito.eq(request), Mockito.isNull()))
                    .thenThrow(exception);

            currencyController.convert(request, null);
            fail("Expected exception was not thrown");
        } catch (IOException e) {
            assertEquals("Test exception", e.getMessage());
        }
    }

    @Test
    public void whenGetExchangeRateWithValidRequest_shouldReturnSuccessResponse() {
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        ExchangeRateResponse mockResponse = new ExchangeRateResponse();

        Mockito.when(currencyService.getExchangeRate(Mockito.eq(sourceCurrency), Mockito.eq(targetCurrency)))
                .thenReturn(mockResponse);

        ResponseEntity<ExchangeRateResponse> response = currencyController.getExchangeRate(sourceCurrency, targetCurrency);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenGetExchangeRateThrowsException_shouldHandleErrorGracefully() {
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        RuntimeException exception = new RuntimeException("Test exception");

        try {
            Mockito.when(currencyService.getExchangeRate(Mockito.eq(sourceCurrency), Mockito.eq(targetCurrency)))
                    .thenThrow(exception);

            currencyController.getExchangeRate(sourceCurrency, targetCurrency);
            fail("Expected exception was not thrown");
        } catch (RuntimeException e) {
            assertEquals("Test exception", e.getMessage());
        }
    }

    @Test
    public void whenGetHistoryWithValidTransactionId_shouldReturnSuccessResponse() {
        String transactionId = "txn123";
        Pageable pageable = Mockito.mock(Pageable.class);
        HistoryResponse mockResponse = Mockito.mock(HistoryResponse.class);

        Mockito.when(currencyService.getHistory(Mockito.eq(transactionId), Mockito.isNull(), Mockito.eq(pageable)))
                .thenReturn(mockResponse);

        ResponseEntity<HistoryResponse> response = currencyController.getHistory(transactionId, null, pageable);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenGetHistoryWithValidDate_shouldReturnSuccessResponse() {
        LocalDate date = LocalDate.of(2023, 10, 25);
        Pageable pageable = Mockito.mock(Pageable.class);
        HistoryResponse mockResponse = Mockito.mock(HistoryResponse.class);

        Mockito.when(currencyService.getHistory(Mockito.isNull(), Mockito.eq(date), Mockito.eq(pageable)))
                .thenReturn(mockResponse);

        ResponseEntity<HistoryResponse> response = currencyController.getHistory(null, date, pageable);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenGetHistoryWithoutCriteria_shouldThrowMissingSearchCriteriaException() {
        Pageable pageable = Mockito.mock(Pageable.class);

        try {
            currencyController.getHistory(null, null, pageable);
            fail("Expected MissingSearchCriteriaException was not thrown");
        } catch (MissingSearchCriteriaException e) {
            assertNotNull(e);
        }
    }
}