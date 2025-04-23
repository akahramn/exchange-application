package com.abdullahkahraman.exchange.client;

import com.abdullahkahraman.exchange.dto.CurrencyLayerResponse;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
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
public class CurrencyLayerClient {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyLayerClient.class);

    private final RestTemplate restTemplate;
    @Value("${currencylayer.api.url}")
    private String CURRENCY_LAYER_API_URL;
    @Value("${currencylayer.api.access-key}")
    private String CURRENCY_LAYER_ACCESS_KEY;

    /**
     * Fetches the exchange rate between two currencies using a specified key.
     *
     * @param source the source currency code
     * @param target the target currency code
     * @param key the key used to identify the specific exchange rate in the response
     * @return the exchange rate as a Double
     * @throws RateNotFoundException if the exchange rate for the given key is not found
     */
    public Double fetchExchangeRate(CurrencyCode source, CurrencyCode target, String key) {
        List<String> symbols = List.of(target.toString());
        String joinedSymbols = String.join(",", symbols);

        String url = String.format(
                "%s?access_key=%s&currencies=%s&source=%s&format=1",
                CURRENCY_LAYER_API_URL, CURRENCY_LAYER_ACCESS_KEY, joinedSymbols, source
        );

        try {
            logger.info("Fetching exchange rate from external API: {} -> {}", source, target);
            logger.debug("Currency Layer request URL: {}", url);

            ResponseEntity<CurrencyLayerResponse> entity = restTemplate.getForEntity(url, CurrencyLayerResponse.class);
            CurrencyLayerResponse response = entity.getBody();

            if (response == null || response.getQuotes() == null) {
                logger.error("Currency Layer API returned null or invalid response for key: {}", key);
                throw new RateNotFoundException(key);
            }

            Double rate = response.getQuotes().get(key);

            if (rate == null) {
                logger.warn("Exchange rate not found in API response for key: {}", key);
                throw new RateNotFoundException(key);
            }

            logger.info("Successfully fetched exchange rate for key='{}': {}", key, rate);
            return rate;

        } catch (Exception e) {
            logger.error("Failed to fetch exchange rate from external API for key='{}': {}", key, e.getMessage(), e);
            throw e;
        }
    }
}
