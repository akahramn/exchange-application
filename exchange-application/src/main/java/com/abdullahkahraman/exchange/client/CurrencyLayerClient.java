package com.abdullahkahraman.exchange.client;

import com.abdullahkahraman.exchange.dto.CurrencyLayerResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.RateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrencyLayerClient {
    private final RestTemplate restTemplate;
    @Value("${currencylayer.api.url}")
    private String CURRENCY_LAYER_API_URL;
    @Value("${currencylayer.api.access-key}")
    private String CURRENCY_LAYER_ACCESS_KEY;

    public Double fetchExchangeRate(CurrencyCode source, CurrencyCode target, String key) {
        List<String> symbols = List.of(target.toString());
        String joinedSymbols = String.join(",", symbols);
        String url = String.format(
                "%s?access_key=%s&currencies=%s&source=%s&format=1",
                CURRENCY_LAYER_API_URL, CURRENCY_LAYER_ACCESS_KEY, joinedSymbols, source
        );

        ResponseEntity<CurrencyLayerResponse> entity = restTemplate.getForEntity(url, CurrencyLayerResponse.class);
        CurrencyLayerResponse response = entity.getBody();
        Double rate = response.getQuotes().get(key);
        if (rate == null) {
            throw new RateNotFoundException(key);
        }
        return rate;
    }
}
