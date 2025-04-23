package com.abdullahkahraman.exchange.client;

import com.abdullahkahraman.exchange.dto.CurrencyLayerResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.RateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CurrencyLayerClientTest {
    private CurrencyLayerClient currencyLayerClient;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws Exception {
        restTemplate = Mockito.mock(RestTemplate.class);
        currencyLayerClient = new CurrencyLayerClient(restTemplate);

        setField(currencyLayerClient, "CURRENCY_LAYER_API_URL", "http://fake-api.com");
        setField(currencyLayerClient, "CURRENCY_LAYER_ACCESS_KEY", "test-access-key");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void whenApiRespondsWithQuote_shouldReturnRate() {
        CurrencyCode source = CurrencyCode.USD;
        CurrencyCode target = CurrencyCode.EUR;
        String key = "USDEUR";

        CurrencyLayerResponse response = new CurrencyLayerResponse();
        response.setQuotes(Map.of(key, 37.99));

        ResponseEntity<CurrencyLayerResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(CurrencyLayerResponse.class)))
                .thenReturn(entity);

        Double result = currencyLayerClient.fetchExchangeRate(source, target, key);

        assertNotNull(result);
        assertEquals(37.99, result);
    }

    @Test
    void whenApiRespondsWithNullBody_shouldThrowException() {
        CurrencyCode source = CurrencyCode.USD;
        CurrencyCode target = CurrencyCode.EUR;
        ResponseEntity<CurrencyLayerResponse> entity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(CurrencyLayerResponse.class)))
                .thenReturn(entity);

        assertThrows(RateNotFoundException.class, () -> currencyLayerClient.fetchExchangeRate(source, target, "USDEUR"));
    }

    @Test
    void whenApiResponseDoesNotContainKey_shouldThrowRateNotFoundException() {
        CurrencyCode source = CurrencyCode.USD;
        CurrencyCode target = CurrencyCode.EUR;
        String key = "USDEUR";

        CurrencyLayerResponse response = new CurrencyLayerResponse();
        response.setQuotes(Map.of());

        ResponseEntity<CurrencyLayerResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(CurrencyLayerResponse.class)))
                .thenReturn(entity);

        assertThrows(RateNotFoundException.class, () -> currencyLayerClient.fetchExchangeRate(source, target, key));
    }

    @Test
    void whenApiRespondsWithNonOkHttpStatus_shouldThrowException() {
        CurrencyCode source = CurrencyCode.USD;
        CurrencyCode target = CurrencyCode.EUR;

        ResponseEntity<CurrencyLayerResponse> entity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(anyString(), eq(CurrencyLayerResponse.class)))
                .thenReturn(entity);

        assertThrows(Exception.class, () -> currencyLayerClient.fetchExchangeRate(source, target, "USDEUR"));
    }
}