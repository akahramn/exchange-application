package com.abdullahkahraman.exchange.client;

import com.abdullahkahraman.exchange.dto.FixerApiResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.CurrencyRateFetchException;
import com.abdullahkahraman.exchange.exception.RateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FixerApiClient {

    private static final Logger logger = LoggerFactory.getLogger(FixerApiClient.class);
    @Value("${fixer.api.url}")
    private String FIXER_API_URL;
    @Value("${fixer.api.access-key}")
    private String FIXER_API_ACCESS_KEY;
    private final RestTemplate restTemplate;

    public Double fetchExchangeRate(CurrencyCode source, CurrencyCode target, String key) {
        if (!CurrencyCode.EUR.equals(source)) {
            throw new UnsupportedOperationException("Fixer API only supports EUR as base currency (in free tier)");
        }

        CurrencyCode symbols = target;
        String url = String.format("%s?access_key=%s&symbols=%s&format=1", FIXER_API_URL, FIXER_API_ACCESS_KEY, symbols);

        try {
            logger.info("Calling Fixer API: {}", url);
            ResponseEntity<FixerApiResponse> responseEntity = restTemplate.getForEntity(url, FixerApiResponse.class);

            FixerApiResponse response = responseEntity.getBody();

            if (response == null || !response.isSuccess()) {
                throw new CurrencyRateFetchException(source.toString(), target.toString());
            }

            Double rate = response.getRates().get(target.toString());

            if (rate == null) {
                throw new RateNotFoundException(key);
            }

            logger.info("Fixer API rate fetched: EUR → {} = {}", target.toString(), rate);

            return rate;

        } catch (Exception e) {
            logger.error("Fixer API call failed: {}", e.getMessage(), e);
            throw new CurrencyRateFetchException(source.toString(), target.toString());
        }
    }
}
