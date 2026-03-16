package com.horadoshow.robot.marketdata;

import java.util.Locale;

/**
 * `brapi.dev` uses Yahoo-like ranges such as: 5d, 1mo, 3mo, 6mo, 1y, 2y, 5y, 10y, max
 */
public enum MarketDataRange {
  DAYS_5("5d"),
  MONTH_1("1mo"),
  MONTHS_3("3mo"),
  MONTHS_6("6mo"),
  YEAR_1("1y"),
  YEARS_2("2y"),
  YEARS_5("5y"),
  YEARS_10("10y"),
  MAX("max");

  private final String apiValue;

  MarketDataRange(String apiValue) {
    this.apiValue = apiValue;
  }

  public String apiValue() {
    return apiValue;
  }

  public static MarketDataRange parse(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("range is required (examples: 6mo, 1y, 2y)");
    }
    String v = value.trim().toLowerCase(Locale.ROOT);
    for (MarketDataRange r : values()) {
      if (r.apiValue.equals(v)) {
        return r;
      }
    }
    throw new IllegalArgumentException("Unsupported range: " + value + " (try: 6mo, 1y, 2y, 5y, max)");
  }
}

