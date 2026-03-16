package com.showtime.robot.analysis;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.showtime.robot.domain.Candle;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class IndicatorsTest {
  @Test
  void rsiIsHighOnUptrend() {
    List<Candle> candles = new ArrayList<>();
    Instant t = Instant.parse("2025-01-01T00:00:00Z");
    double price = 10.0;
    for (int i = 0; i < 40; i++) {
      price += 0.2;
      candles.add(
          new Candle(
              t.plusSeconds(86400L * i),
              BigDecimal.valueOf(price - 0.1),
              BigDecimal.valueOf(price + 0.2),
              BigDecimal.valueOf(price - 0.3),
              BigDecimal.valueOf(price),
              1000L,
              BigDecimal.valueOf(price)));
    }

    double rsi = Indicators.rsiClose(candles, 14).orElseThrow();
    assertTrue(rsi > 60.0, "Expected RSI to be > 60 but was " + rsi);
  }
}
