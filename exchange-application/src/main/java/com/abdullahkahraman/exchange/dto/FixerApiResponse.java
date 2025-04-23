package com.abdullahkahraman.exchange.dto;

import lombok.Data;

import java.util.Map;
@Data
public class FixerApiResponse {
    private boolean success;
    private String base;
    private String date;
    private Map<String, Double> rates;
}
