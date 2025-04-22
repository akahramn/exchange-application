package com.abdullahkahraman.exchange.dto;

import lombok.Data;

import java.util.Map;
@Data
public class CurrencyLayerResponse {
    private boolean success;
    private String terms;
    private String privacy;
    private long timestamp;
    private String source;
    private Map<String, Double> quotes;
}
