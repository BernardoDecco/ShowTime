package com.horadoshow.robot.marketdata;

import com.horadoshow.robot.marketdata.brapi.BrapiMarketDataClient;

public final class MarketDataClients {
  private MarketDataClients() {}

  public static MarketDataClient brapi() {
    return new BrapiMarketDataClient();
  }
}

