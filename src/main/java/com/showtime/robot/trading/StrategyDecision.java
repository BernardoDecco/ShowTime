package com.showtime.robot.trading;

public record StrategyDecision(
    DecisionType type, Double positionSizePct, Double stopLossPct, Double takeProfitPct) {

  public static StrategyDecision hold() {
    return new StrategyDecision(DecisionType.HOLD, null, null, null);
  }

  public static StrategyDecision buy(
      double positionSizePct, double stopLossPct, double takeProfitPct) {
    return new StrategyDecision(DecisionType.BUY, positionSizePct, stopLossPct, takeProfitPct);
  }

  public static StrategyDecision sellAll() {
    return new StrategyDecision(DecisionType.SELL, 1.0, null, null);
  }
}
