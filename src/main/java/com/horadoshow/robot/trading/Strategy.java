package com.horadoshow.robot.trading;

/**
 * Defines the contract for a trading strategy.
 * <p>
 * Implementations receive a multi-timeframe market snapshot and the current account context,
 * and must return a {@link StrategyDecision} describing what to do at this step.
 */
public interface Strategy {

  /**
   * Called for each new bar (for example a new daily candle).
   *
   * @param snapshot current multi-timeframe market data for a symbol
   * @param context  current account and open position information
   * @return a {@link StrategyDecision} describing the desired action
   */
  StrategyDecision onBar(MultiTimeframeSnapshot snapshot, StrategyContext context);
}

