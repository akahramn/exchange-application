package com.abdullahkahraman.exchange.controller;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.dto.CurrencyConversionResponse;
import com.abdullahkahraman.exchange.dto.ExchangeRateResponse;
import com.abdullahkahraman.exchange.dto.HistoryResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.InvalidCsvFormatException;
import com.abdullahkahraman.exchange.exception.MissingSearchCriteriaException;
import com.abdullahkahraman.exchange.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionControllerTest {

    private TransactionService transactionService;
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        transactionService = Mockito.mock(TransactionService.class);
        transactionController = new TransactionController(transactionService);
    }

    @Test
    public void whenConvertCurrencyWithValidRequest_shouldReturnSuccessResponse() throws Exception {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        List<CurrencyConversionResponse> mockResponse = List.of(new CurrencyConversionResponse());

        Mockito.when(transactionService.convertCurrency(Mockito.eq(request), Mockito.isNull()))
                .thenReturn(mockResponse);

        ResponseEntity<List<CurrencyConversionResponse>> response = transactionController.convert(request, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenConvertCurrencyWithFile_shouldReturnSuccessResponse() throws Exception {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        List<CurrencyConversionResponse> mockResponse = List.of(new CurrencyConversionResponse());

        Mockito.when(transactionService.convertCurrency(Mockito.isNull(), Mockito.eq(mockFile)))
                .thenReturn(mockResponse);

        ResponseEntity<List<CurrencyConversionResponse>> response = transactionController.convert(null, mockFile);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenConvertCurrencyThrowsException_shouldHandleErrorGracefully() {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        InvalidCsvFormatException exception = new InvalidCsvFormatException("Error reading CSV file");

        try {
            Mockito.when(transactionService.convertCurrency(Mockito.eq(request), Mockito.isNull()))
                    .thenThrow(exception);

            transactionController.convert(request, null);
            fail("Expected exception was not thrown");
        } catch (InvalidCsvFormatException e) {
            assertEquals("Error reading CSV file", e.getMessage());
        }
    }

    @Test
    public void whenGetExchangeRateWithValidRequest_shouldReturnSuccessResponse() {
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        ExchangeRateResponse mockResponse = new ExchangeRateResponse();

        Mockito.when(transactionService.getExchangeRate(Mockito.eq(sourceCurrency.toString()), Mockito.eq(targetCurrency.toString())))
                .thenReturn(mockResponse);

        ResponseEntity<ExchangeRateResponse> response = transactionController.getExchangeRate(sourceCurrency.toString(), targetCurrency.toString());

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
            Mockito.when(transactionService.getExchangeRate(Mockito.eq(sourceCurrency.toString()), Mockito.eq(targetCurrency.toString())))
                    .thenThrow(exception);

            transactionController.getExchangeRate(sourceCurrency.toString(), targetCurrency.toString());
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

        Mockito.when(transactionService.getHistory(Mockito.eq(transactionId), Mockito.isNull(), Mockito.eq(pageable)))
                .thenReturn(mockResponse);

        ResponseEntity<HistoryResponse> response = transactionController.getHistory(transactionId, null, pageable);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenGetHistoryWithValidDate_shouldReturnSuccessResponse() {
        LocalDate date = LocalDate.of(2023, 10, 25);
        Pageable pageable = Mockito.mock(Pageable.class);
        HistoryResponse mockResponse = Mockito.mock(HistoryResponse.class);

        Mockito.when(transactionService.getHistory(Mockito.isNull(), Mockito.eq(date), Mockito.eq(pageable)))
                .thenReturn(mockResponse);

        ResponseEntity<HistoryResponse> response = transactionController.getHistory(null, date, pageable);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void whenGetHistoryWithoutCriteria_shouldThrowMissingSearchCriteriaException() {
        Pageable pageable = Mockito.mock(Pageable.class);

        try {
            transactionController.getHistory(null, null, pageable);
            fail("Expected MissingSearchCriteriaException was not thrown");
        } catch (MissingSearchCriteriaException e) {
            assertNotNull(e);
        }
    }
}