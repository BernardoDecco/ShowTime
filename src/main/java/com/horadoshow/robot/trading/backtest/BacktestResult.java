package com.horadoshow.robot.trading.backtest;

import java.math.BigDecimal;

public record BacktestResult(
    BigDecimal initialCash,
    BigDecimal finalCash,
    BigDecimal equity,
    int trades
) {

  public BigDecimal totalReturnPct() {
    if (initialCash.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return equity.subtract(initialCash)
        .divide(initialCash, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }
}

