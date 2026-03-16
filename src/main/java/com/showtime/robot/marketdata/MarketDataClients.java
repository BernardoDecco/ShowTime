package com.showtime.robot.marketdata;

import com.showtime.robot.marketdata.brapi.BrapiMarketDataClient;

public final class MarketDataClients {
  private MarketDataClients() {}

  public static MarketDataClient brapi() {
    return new BrapiMarketDataClient();
  }
}
