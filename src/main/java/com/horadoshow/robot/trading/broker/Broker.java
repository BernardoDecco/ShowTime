package com.horadoshow.robot.trading.broker;

import java.time.Instant;

public interface Broker {

  void placeBuy(String symbol, double quantity, Instant time);

  void placeSell(String symbol, double quantity, Instant time);

  AccountState accountState();
}

