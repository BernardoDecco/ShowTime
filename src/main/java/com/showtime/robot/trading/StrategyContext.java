package com.showtime.robot.trading;

import java.math.BigDecimal;

public record StrategyContext(
    BigDecimal cash, BigDecimal positionQuantity, BigDecimal positionAveragePrice) {

  public boolean hasPosition() {
    return positionQuantity != null && positionQuantity.compareTo(BigDecimal.ZERO) > 0;
  }
}
