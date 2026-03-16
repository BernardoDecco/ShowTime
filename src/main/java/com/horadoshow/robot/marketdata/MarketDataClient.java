package com.horadoshow.robot.marketdata;

import com.horadoshow.robot.domain.Candle;
import java.util.List;

public interface MarketDataClient {
  /**
   * Returns daily candles in ascending time order (oldest -> newest).
   */
  List<Candle> getDailyCandles(String symbol, MarketDataRange range);
}

