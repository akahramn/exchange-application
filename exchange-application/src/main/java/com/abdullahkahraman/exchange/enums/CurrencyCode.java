package com.abdullahkahraman.exchange.enums;

import java.util.Arrays;

public enum CurrencyCode {
    USD("United States Dollar"),
    EUR("Euro"),
    TRY("Turkish Lira"),
    GBP("British Pound"),
    JPY("Japanese Yen"),
    AUD("Australian Dollar"),
    CAD("Canadian Dollar"),
    CHF("Swiss Franc"),
    CNH("Chinese Yuan (Offshore)"),
    HKD("Hong Kong Dollar"),
    NZD("New Zealand Dollar");

    private final String description;

    CurrencyCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String code) {
        return Arrays.stream(CurrencyCode.values())
                .anyMatch(currency -> currency.name().equalsIgnoreCase(code));
    }

    public static CurrencyCode fromString(String code) {
        return Arrays.stream(CurrencyCode.values())
                .filter(currency -> currency.name().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid currency code: " + code));
    }
}
