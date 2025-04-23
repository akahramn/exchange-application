package com.abdullahkahraman.exchange.enums;

import java.util.Arrays;

/**
 * Represents an enumeration of supported currency codes along with their
 * respective full descriptions. Commonly used in currency exchange and
 * conversion functionalities.
 *
 * This enum provides functionality to retrieve full descriptions of the
 * currency codes and verify the validity of a currency code.
 */
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

    public static boolean isValid(String code) {
        return Arrays.stream(CurrencyCode.values())
                .anyMatch(currency -> currency.name().equalsIgnoreCase(code));
    }
}
