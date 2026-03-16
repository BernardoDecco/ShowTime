package com.horadoshow.robot.broker;

import java.math.BigDecimal;
import java.util.Optional;

public record OrderRequest(
    String symbol,
    OrderSide side,
    OrderType type,
    BigDecimal quantity,
    Optional<BigDecimal> limitPrice,
    Optional<Bracket> bracket
) {
  public OrderRequest {
    if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("symbol is required");
    if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("quantity must be > 0");
    limitPrice = limitPrice == null ? Optional.empty() : limitPrice;
    bracket = bracket == null ? Optional.empty() : bracket;
  }
}

