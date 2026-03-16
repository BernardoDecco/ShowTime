package com.horadoshow.robot.trading.broker;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SimulatedBroker implements Broker {

  private BigDecimal cash;
  private final Map<String, Position> positions = new HashMap<>();

  public SimulatedBroker(BigDecimal initialCash) {
    this.cash = initialCash;
  }

  @Override
  public void placeBuy(String symbol, double quantity, Instant time) {
    // price will be applied externally in backtest engine
  }

  @Override
  public void placeSell(String symbol, double quantity, Instant time) {
    // price will be applied externally in backtest engine
  }

  public void applyTrade(String symbol, BigDecimal tradePrice, BigDecimal quantityChange) {
    Position existing = positions.get(symbol);
    if (quantityChange.compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal cost = tradePrice.multiply(quantityChange);
      cash = cash.subtract(cost);
      if (existing == null) {
        positions.put(symbol, new Position(symbol, quantityChange, tradePrice));
      } else {
        BigDecimal newQty = existing.quantity().add(quantityChange);
        BigDecimal newAvg = existing.averagePrice()
            .multiply(existing.quantity())
            .add(tradePrice.multiply(quantityChange))
            .divide(newQty, existing.averagePrice().scale(), BigDecimal.ROUND_HALF_UP);
        positions.put(symbol, new Position(symbol, newQty, newAvg));
      }
    } else if (quantityChange.compareTo(BigDecimal.ZERO) < 0) {
      if (existing == null) {
        return;
      }
      BigDecimal absQty = quantityChange.abs();
      BigDecimal proceeds = tradePrice.multiply(absQty);
      cash = cash.add(proceeds);
      BigDecimal remaining = existing.quantity().subtract(absQty);
      if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
        positions.remove(symbol);
      } else {
        positions.put(symbol, new Position(symbol, remaining, existing.averagePrice()));
      }
    }
  }

  @Override
  public AccountState accountState() {
    return new AccountState(cash, Collections.unmodifiableMap(positions));
  }
}

