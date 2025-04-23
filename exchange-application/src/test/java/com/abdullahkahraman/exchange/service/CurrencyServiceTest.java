package com.abdullahkahraman.exchange.service;

import com.abdullahkahraman.exchange.cache.CurrencyCacheService;
import com.abdullahkahraman.exchange.client.CurrencyLayerClient;
import com.abdullahkahraman.exchange.dto.*;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.CurrencyRateFetchException;
import com.abdullahkahraman.exchange.exception.InvalidCsvFormatException;
import com.abdullahkahraman.exchange.model.Currency;
import com.abdullahkahraman.exchange.parser.ConversionFileParser;
import com.abdullahkahraman.exchange.parser.ConversionFileParserFactory;
import com.abdullahkahraman.exchange.repository.CurrencyRepository;
import com.abdullahkahraman.exchange.validator.CurrencyCodeValidator;
import com.abdullahkahraman.exchange.validator.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyServiceTest {

    private ConversionFileParserFactory conversionFileParserFactory;
    private CurrencyService currencyService;
    private CurrencyLayerClient currencyLayerClient;
    private CurrencyRepository currencyRepository;
    private CurrencyCacheService currencyCacheService;
    private ConversionFileParser conversionFileParser;
    private Validator validator1;
    private Validator validator2;
    private CurrencyCodeValidator currencyCodeValidator;

    @BeforeEach
    void setUp() {
        conversionFileParser = Mockito.mock(ConversionFileParser.class);
        currencyLayerClient = Mockito.mock(CurrencyLayerClient.class);
        currencyRepository = Mockito.mock(CurrencyRepository.class);
        currencyCacheService = Mockito.mock(CurrencyCacheService.class);
        validator1 = Mockito.mock(Validator.class);
        validator2 = Mockito.mock(Validator.class);
        conversionFileParserFactory = Mockito.mock(ConversionFileParserFactory.class);
        currencyCodeValidator = Mockito.mock(CurrencyCodeValidator.class);
        currencyService = new CurrencyService(currencyRepository,
                currencyCacheService,
                currencyLayerClient,
                conversionFileParserFactory,
                List.of(validator1, validator2), currencyCodeValidator);
    }

    @Test
    public void whenRateExistsInCache_shouldReturnCachedRate() {
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        String key = sourceCurrency.toString() + targetCurrency.toString();

        Mockito.when(currencyCacheService.exists(key)).thenReturn(true);
        Mockito.when(currencyCacheService.getRate(key)).thenReturn(38.0);
        Double rate = currencyService.getRate(sourceCurrency, targetCurrency);

        assertNotNull(rate);
        assertEquals(38.0, rate);

        Mockito.verify(currencyCacheService).exists(key);
        Mockito.verify(currencyCacheService).getRate(key);


    }

    @Test
    void whenRateDoesNotExistInCache_shouldFetchFromClientAndCache() {
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        String key = sourceCurrency.toString() + targetCurrency.toString();
        Double fetchedRate = 38.0;

        when(currencyCacheService.exists(key)).thenReturn(false);
        when(currencyLayerClient.fetchExchangeRate(sourceCurrency, targetCurrency, key)).thenReturn(fetchedRate);

        Double rate = currencyService.getRate(sourceCurrency, targetCurrency);

        assertNotNull(rate);
        assertEquals(fetchedRate, rate);

        verify(currencyCacheService).exists(key);
        verify(currencyLayerClient).fetchExchangeRate(sourceCurrency, targetCurrency, key);
    }

    @Test
    void whenRateFetchFailsAndFallbackExistsInCache_shouldReturnFallbackRate() {
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        String key = sourceCurrency.toString() + targetCurrency.toString();
        Double fallbackRate = 38.0;

        when(currencyCacheService.exists(key)).thenReturn(false);
        when(currencyLayerClient.fetchExchangeRate(sourceCurrency, targetCurrency, key)).thenThrow(new RuntimeException("Fetch failed"));
        when(currencyCacheService.getRate(key)).thenReturn(fallbackRate);

        Double rate = currencyService.getRate(sourceCurrency, targetCurrency);

        assertNotNull(rate);
        assertEquals(fallbackRate, rate);

        verify(currencyCacheService).getRate(key);
        verify(currencyLayerClient).fetchExchangeRate(sourceCurrency, targetCurrency, key);
    }

    @Test
    void whenRateFetchFailsAndFallbackDoesNotExist_shouldThrowCurrencyRateFetchException() {
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        String key = sourceCurrency.toString() + targetCurrency.toString();

        when(currencyCacheService.exists(key)).thenReturn(false);
        when(currencyLayerClient.fetchExchangeRate(sourceCurrency, targetCurrency, key)).thenThrow(new RuntimeException("Fetch failed"));
        when(currencyCacheService.getRate(key)).thenReturn(null);

        assertThrows(CurrencyRateFetchException.class, () -> currencyService.getRate(sourceCurrency, targetCurrency));

        verify(currencyCacheService).getRate(key);
    }

    @Test
    void whenExchangeRateExists_shouldReturnExchangeRateResponse() {
        String sourceCurrency = "USD";
        String targetCurrency = "EUR";
        String key = sourceCurrency + targetCurrency;
        Double rate = 42.0;

        when(currencyCacheService.exists(key)).thenReturn(true);
        when(currencyCacheService.getRate(key)).thenReturn(rate);

        ExchangeRateResponse response = currencyService.getExchangeRate(sourceCurrency, targetCurrency);

        assertNotNull(response);
        assertEquals(sourceCurrency, response.getSourceCurrency().toString());
        assertEquals(targetCurrency, response.getTargetCurrency().toString());
        assertEquals(rate, response.getRate());

        verify(currencyCacheService).exists(key);
        verify(currencyCacheService).getRate(key);
    }

    @Test
    void whenExchangeRateCannotBeFetched_shouldThrowCurrencyRateFetchException() {
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        String key = sourceCurrency.toString() + targetCurrency.toString();

        when(currencyCacheService.exists(key)).thenReturn(false);
        when(currencyLayerClient.fetchExchangeRate(sourceCurrency, targetCurrency, key))
                .thenThrow(new RuntimeException("Fetch failed"));
        when(currencyCacheService.getRate(key)).thenReturn(null);

        assertThrows(CurrencyRateFetchException.class, () -> currencyService.getExchangeRate(sourceCurrency.toString(), targetCurrency.toString()));

        verify(currencyCacheService).getRate(key);
        verify(currencyLayerClient).fetchExchangeRate(sourceCurrency, targetCurrency, key);
    }

    @Test
    void whenTransactionIdProvided_shouldReturnHistoryResponse() {
        String transactionId = "abc123";
        LocalDate date = null;
        Pageable pageable = PageRequest.of(0, 10);
        List<Currency> currencyEntities = new ArrayList<>();
        currencyEntities.add(new Currency(
                "tx123", CurrencyCode.USD, CurrencyCode.TRY, new BigDecimal("100"), new BigDecimal("3245.60"), LocalDateTime.now()
        ));
        Page<Currency> currencyPage = new PageImpl<>(currencyEntities, pageable, 1);

        HistoryResponse expectedResponse = new HistoryResponse(
                currencyPage.map(entity -> new CurrencyDto(
                        entity.getTransactionId(),
                        entity.getSourceCurrency().name(),
                        entity.getTargetCurrency().name(),
                        entity.getResult()
                ))
        );

        when(currencyRepository.existsById(transactionId)).thenReturn(true);
        when(currencyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(currencyPage);

        HistoryResponse result = currencyService.getHistory(transactionId, date, pageable);

        assertNotNull(expectedResponse);
        assertEquals(expectedResponse, result);

        verify(currencyRepository).existsById(transactionId);
        verify(currencyRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void whenDateProvided_shouldReturnHistoryResponse() {
        String transactionId = null;
        LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<Currency> currencyEntities = new ArrayList<>();
        currencyEntities.add(new Currency(
                "tx123", CurrencyCode.USD, CurrencyCode.TRY, new BigDecimal("100"), new BigDecimal("3245.60"), LocalDateTime.now()
        ));
        Page<Currency> currencyPage = new PageImpl<>(currencyEntities, pageable, 1);

        HistoryResponse expectedResponse = new HistoryResponse(
                currencyPage.map(entity -> new CurrencyDto(
                        entity.getTransactionId(),
                        entity.getSourceCurrency().name(),
                        entity.getTargetCurrency().name(),
                        entity.getResult()
                ))
        );

        when(currencyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(currencyPage);

        HistoryResponse result = currencyService.getHistory(transactionId, date, pageable);

        assertNotNull(expectedResponse);
        assertEquals(result, expectedResponse);

        verify(currencyRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void whenValidAmountAndRateProvided_shouldCalculateCorrectAmount() {
        BigDecimal amount = BigDecimal.valueOf(100.00);
        Double rate = 1.5;

        BigDecimal result = CurrencyService.calculateAmount(amount, rate);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.0000).setScale(4, RoundingMode.HALF_UP), result);
    }

    @Test
    void whenAmountOrRateIsNull_shouldThrowIllegalArgumentException() {
        BigDecimal amount = null;
        Double rate = 1.5;

        BigDecimal finalAmount = amount;
        Double finalRate = rate;
        assertThrows(IllegalArgumentException.class, () -> CurrencyService.calculateAmount(finalAmount, finalRate));

        amount = BigDecimal.valueOf(100.00);
        rate = null;

        BigDecimal finalAmount1 = amount;
        Double finalRate1 = rate;
        assertThrows(IllegalArgumentException.class, () -> CurrencyService.calculateAmount(finalAmount1, finalRate1));
    }

    @Test
    public void whenFileContainsValidRequests_shouldReturnConversionResponses() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", "dummy content".getBytes()
        );

        List<CurrencyConversionRequest> mockRequests = List.of(
                new CurrencyConversionRequest(BigDecimal.valueOf(100), CurrencyCode.USD.toString(), CurrencyCode.EUR.toString()),
                new CurrencyConversionRequest(BigDecimal.valueOf(200), CurrencyCode.GBP.toString(), CurrencyCode.USD.toString())
        );

        List<CurrencyConversionResponse> mockResponses = List.of(
                new CurrencyConversionResponse("tx1", CurrencyCode.USD, CurrencyCode.EUR, BigDecimal.valueOf(100), BigDecimal.valueOf(110)),
                new CurrencyConversionResponse("tx2", CurrencyCode.GBP, CurrencyCode.USD, BigDecimal.valueOf(200), BigDecimal.valueOf(260))
        );

        when(conversionFileParserFactory.getParser(file)).thenReturn(conversionFileParser);
        when(conversionFileParser.parse(file)).thenReturn(mockRequests);

        CurrencyService spyService = spy(currencyService);

        doReturn(mockResponses.get(0))
                .doReturn(mockResponses.get(1))
                .when(spyService)
                .convertSingleCurrency(any(CurrencyConversionRequest.class));

        List<CurrencyConversionResponse> result = spyService.convertFileCurrency(file);

        assertEquals(2, result.size());
        assertEquals(mockResponses, result);

        verify(spyService, times(2)).convertSingleCurrency(any());
    }

    @Test
    void whenFileIsEmpty_shouldReturnEmptyResponse() {
        MultipartFile file = mock(MultipartFile.class);

        when(conversionFileParserFactory.getParser(any(MultipartFile.class))).thenReturn(conversionFileParser);
        when(conversionFileParser.parse(file)).thenReturn(new ArrayList<>());

        List<CurrencyConversionResponse> responses = currencyService.convertFileCurrency(file);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(conversionFileParserFactory).getParser(file);
        verify(conversionFileParser).parse(file);
    }

    @Test
    public void whenFileHasInvalidFormat_shouldThrowIOException() {
        MultipartFile file = mock(MultipartFile.class);

        when(conversionFileParserFactory.getParser(any(MultipartFile.class))).thenReturn(conversionFileParser);
        when(conversionFileParser.parse(file)).thenThrow(new InvalidCsvFormatException("Error reading CSV file"));

        assertThrows(InvalidCsvFormatException.class, () -> currencyService.convertFileCurrency(file));

        verify(conversionFileParserFactory).getParser(file);
        verify(conversionFileParser).parse(file);
    }

    @Test
    public void whenSaveTransactionValidRequestProvided_shouldReturnResponses() {
        String transactionId = "tx1";
        CurrencyConversionRequest request = new CurrencyConversionRequest(BigDecimal.valueOf(100), CurrencyCode.USD.toString(), CurrencyCode.EUR.toString());
        BigDecimal result = BigDecimal.valueOf(110);

        Currency currency = Currency.builder()
                .transactionId(transactionId)
                .sourceCurrency(CurrencyCode.valueOf(request.getSourceCurrency()))
                .targetCurrency(CurrencyCode.valueOf(request.getTargetCurrency()))
                .amount(request.getAmount())
                .result(result)
                .build();

        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        currencyService.saveTransaction(transactionId, request, result);

        verify(currencyRepository).save(any(Currency.class));
    }

    @Test
    public void whenConvertCurrencyValidRequestProvided_shouldConvertSingleSuccessfully() {
        CurrencyConversionRequest request = new CurrencyConversionRequest(
                BigDecimal.valueOf(100), CurrencyCode.USD.toString(), CurrencyCode.EUR.toString());
        Double rate = 1.5;

        String key = "USDEUR";
        when(currencyCacheService.exists(key)).thenReturn(true);
        when(currencyCacheService.getRate(key)).thenReturn(rate);

        BigDecimal expectedResult = BigDecimal.valueOf(150.0).setScale(4, RoundingMode.HALF_UP);

        CurrencyConversionResponse response = currencyService.convertSingleCurrency(request);

        assertNotNull(response);
        assertEquals(request.getSourceCurrency(), response.getSourceCurrency().toString());
        assertEquals(request.getTargetCurrency(), response.getTargetCurrency().toString());
        assertEquals(expectedResult, response.getConvertedAmount());
        assertEquals(request.getAmount(), response.getSourceAmount());

        verify(validator1).validate(request);
        verify(validator2).validate(request);
    }

    @Test
    public void whenConvertSingleCurrencyValidatorFails_shouldThrowException() {
        CurrencyConversionRequest request = new CurrencyConversionRequest(
                BigDecimal.valueOf(100), CurrencyCode.USD.toString(), CurrencyCode.EUR.toString());

        doThrow(new IllegalArgumentException("Invalid request")).when(validator1).validate(request);

        assertThrows(IllegalArgumentException.class, () -> currencyService.convertSingleCurrency(request));

        verify(validator1).validate(request);
        verifyNoMoreInteractions(validator2);
    }

    @Test
    public void whenConvertSingleCurrencyConversionSuccessful_shouldCallGetRateSuccessfully() {
        CurrencyConversionRequest request = new CurrencyConversionRequest(
                BigDecimal.valueOf(100), CurrencyCode.USD.toString(), CurrencyCode.EUR.toString());

        Double rate = 1.8;

        String key = "USDEUR";
        when(currencyCacheService.exists(key)).thenReturn(true);
        when(currencyCacheService.getRate(key)).thenReturn(rate);

        currencyService.convertSingleCurrency(request);
    }

    @Test
    void whenConversionSuccessful_shouldSaveTransaction() {
        CurrencyConversionRequest request = new CurrencyConversionRequest(
                BigDecimal.valueOf(200), CurrencyCode.USD.toString(), CurrencyCode.GBP.toString());
        Double rate = 0.8;
        BigDecimal expectedResult = BigDecimal.valueOf(160.00).setScale(4, RoundingMode.HALF_UP);

        String key = "USDGBP";
        when(currencyCacheService.exists(key)).thenReturn(true);
        when(currencyCacheService.getRate(key)).thenReturn(rate);

        currencyService.convertSingleCurrency(request);

        verify(currencyRepository).save(argThat(transaction ->
                request.getSourceCurrency().equals(transaction.getSourceCurrency().toString()) &&
                        request.getTargetCurrency().equals(transaction.getTargetCurrency().toString()) &&
                        expectedResult.equals(transaction.getResult()) &&
                        request.getAmount().equals(transaction.getAmount()) &&
                        transaction.getTransactionId() != null
        ));
    }

    @Test
    void whenConversionSuccessful_shouldReturnCorrectCurrencyConversionResponse() {
        CurrencyConversionRequest request = new CurrencyConversionRequest(
                BigDecimal.valueOf(50), CurrencyCode.EUR.toString(), CurrencyCode.USD.toString());
        Double rate = 1.1;
        BigDecimal expectedResult = BigDecimal.valueOf(55.00).setScale(4, RoundingMode.HALF_UP);


        String key = "EURUSD";
        when(currencyCacheService.exists(key)).thenReturn(true);
        when(currencyCacheService.getRate(key)).thenReturn(rate);

        CurrencyConversionResponse response = currencyService.convertSingleCurrency(request);

        assertNotNull(response);
        assertEquals(request.getSourceCurrency(), response.getSourceCurrency().toString());
        assertEquals(request.getTargetCurrency(), response.getTargetCurrency().toString());
        assertEquals(expectedResult, response.getConvertedAmount());
        assertEquals(request.getAmount(), response.getSourceAmount());
        assertNotNull(response.getTransactionId());
    }

    @Test
    void whenConvertCurrencyOnlyRequestProvided_shouldReturnSingleResponse() {
        CurrencyConversionRequest request = new CurrencyConversionRequest(BigDecimal.valueOf(100), CurrencyCode.USD.toString(), CurrencyCode.EUR.toString());
        CurrencyConversionResponse mockResponse = new CurrencyConversionResponse("tx1", CurrencyCode.USD, CurrencyCode.EUR, BigDecimal.valueOf(100), BigDecimal.valueOf(110));

        CurrencyService spyService = Mockito.spy(currencyService);
        doReturn(mockResponse).when(spyService).convertSingleCurrency(request);

        // Act
        List<CurrencyConversionResponse> result = spyService.convertCurrency(request, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals(mockResponse, result.get(0));
        verify(spyService).convertSingleCurrency(request);
        verify(spyService, never()).convertFileCurrency(any());
    }

    @Test
    void whenConvertCurrencyOnlyFileProvided_shouldReturnFileResponses() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "content".getBytes());
        CurrencyConversionResponse fileResponse = new CurrencyConversionResponse("tx2", CurrencyCode.GBP, CurrencyCode.USD, BigDecimal.valueOf(50), BigDecimal.valueOf(65));

        CurrencyService spyService = Mockito.spy(currencyService);
        doReturn(List.of(fileResponse)).when(spyService).convertFileCurrency(file);

        // Act
        List<CurrencyConversionResponse> result = spyService.convertCurrency(null, file);

        // Assert
        assertEquals(1, result.size());
        assertEquals(fileResponse, result.get(0));
        verify(spyService).convertFileCurrency(file);
        verify(spyService, never()).convertSingleCurrency(any());
    }

    @Test
    void whenConvertCurrencyBothRequestAndFileProvided_shouldReturnBothResponses() {
        // Arrange
        CurrencyConversionRequest request = new CurrencyConversionRequest(BigDecimal.valueOf(100), CurrencyCode.USD.toString(), CurrencyCode.EUR.toString());
        CurrencyConversionResponse response1 = new CurrencyConversionResponse("tx1", CurrencyCode.USD, CurrencyCode.EUR, BigDecimal.valueOf(100), BigDecimal.valueOf(110));

        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "content".getBytes());
        CurrencyConversionResponse response2 = new CurrencyConversionResponse("tx2", CurrencyCode.GBP, CurrencyCode.USD, BigDecimal.valueOf(50), BigDecimal.valueOf(65));

        CurrencyService spyService = Mockito.spy(currencyService);
        doReturn(response1).when(spyService).convertSingleCurrency(request);
        doReturn(List.of(response2)).when(spyService).convertFileCurrency(file);

        // Act
        List<CurrencyConversionResponse> result = spyService.convertCurrency(request, file);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(response1, response2)));
        verify(spyService).convertSingleCurrency(request);
        verify(spyService).convertFileCurrency(file);
    }

    @Test
    void whenConvertCurrencyBothInputsAreNull_shouldReturnEmptyList() {
        List<CurrencyConversionResponse> result = currencyService.convertCurrency(null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}