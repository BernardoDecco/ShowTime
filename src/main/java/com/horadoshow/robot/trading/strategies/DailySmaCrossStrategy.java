package com.horadoshow.robot.trading.strategies;

import com.horadoshow.robot.domain.Candle;
import com.horadoshow.robot.trading.DecisionType;
import com.horadoshow.robot.trading.MultiTimeframeSnapshot;
import com.horadoshow.robot.trading.Strategy;
import com.horadoshow.robot.trading.StrategyContext;
import com.horadoshow.robot.trading.StrategyDecision;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simple example strategy that trades based on a daily close short/long simple moving average crossover.
 * <p>
 * When the short SMA is above the long SMA and there is no open position, it buys a fixed fraction of cash.
 * When the short SMA drops below the long SMA and there is an open position, it exits the position.
 */
public final class DailySmaCrossStrategy implements Strategy {

  private final int shortPeriod;
  private final int longPeriod;
  private final Deque<BigDecimal> shortWindow = new ArrayDeque<>();
  private final Deque<BigDecimal> longWindow = new ArrayDeque<>();

  /**
   * Creates a new daily SMA crossover strategy.
   *
   * @param shortPeriod number of bars in the short moving average window
   * @param longPeriod  number of bars in the long moving average window (must be greater than {@code shortPeriod})
   */
  public DailySmaCrossStrategy(int shortPeriod, int longPeriod) {
    if (shortPeriod <= 0 || longPeriod <= 0 || shortPeriod >= longPeriod) {
      throw new IllegalArgumentException("shortPeriod must be > 0 and < longPeriod");
    }
    this.shortPeriod = shortPeriod;
    this.longPeriod = longPeriod;
  }

  /**
   * Evaluates the current daily bar and returns the trading decision for this step.
   *
   * @param snapshot multi-timeframe snapshot; this strategy currently only uses the daily candle
   * @param context  current account and position state
   * @return a {@link StrategyDecision} indicating buy, sell, or hold
   */
  @Override
  public StrategyDecision onBar(MultiTimeframeSnapshot snapshot, StrategyContext context) {
    Candle daily = snapshot.daily();
    if (daily == null) {
      return StrategyDecision.hold();
    }
    BigDecimal close = daily.close();

    updateWindow(shortWindow, close, shortPeriod);
    updateWindow(longWindow, close, longPeriod);

    if (shortWindow.size() < shortPeriod || longWindow.size() < longPeriod) {
      return StrategyDecision.hold();
    }

    BigDecimal shortSma = average(shortWindow);
    BigDecimal longSma = average(longWindow);

    boolean bullish = shortSma.compareTo(longSma) > 0;

    if (!context.hasPosition() && bullish) {
      return StrategyDecision.buy(0.5, 0.05, 0.10);
    } else if (context.hasPosition() && !bullish) {
      return new StrategyDecision(DecisionType.SELL, 1.0, null, null);
    }

    return StrategyDecision.hold();
  }

  private static void updateWindow(Deque<BigDecimal> window, BigDecimal value, int maxSize) {
    window.addLast(value);
    if (window.size() > maxSize) {
      window.removeFirst();
    }
  }

  private static BigDecimal average(Deque<BigDecimal> window) {
    BigDecimal sum = BigDecimal.ZERO;
    for (BigDecimal v : window) {
      sum = sum.add(v);
    }
    return sum.divide(BigDecimal.valueOf(window.size()), 6, BigDecimal.ROUND_HALF_UP);
  }
}

