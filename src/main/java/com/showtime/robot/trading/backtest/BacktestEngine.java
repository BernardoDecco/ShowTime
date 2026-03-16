package com.showtime.robot.trading.backtest;

import com.showtime.robot.domain.Candle;
import com.showtime.robot.marketdata.MarketDataClient;
import com.showtime.robot.marketdata.MarketDataRange;
import com.showtime.robot.trading.DecisionType;
import com.showtime.robot.trading.MultiTimeframeSnapshot;
import com.showtime.robot.trading.Strategy;
import com.showtime.robot.trading.StrategyContext;
import com.showtime.robot.trading.StrategyDecision;
import com.showtime.robot.trading.broker.AccountState;
import com.showtime.robot.trading.broker.Position;
import com.showtime.robot.trading.broker.SimulatedBroker;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Executes backtests for a single symbol using historical candles provided by a {@link
 * MarketDataClient}.
 *
 * <p>This engine is responsible for driving the clock over historical data, calling the strategy on
 * each bar, and applying the resulting trades to a {@link SimulatedBroker}.
 */
public final class BacktestEngine {

  private final MarketDataClient marketDataClient;

  public BacktestEngine(MarketDataClient marketDataClient) {
    this.marketDataClient = marketDataClient;
  }

  /**
   * Runs a backtest for a single symbol over the given {@link MarketDataRange}.
   *
   * @param symbol the instrument symbol (for example {@code PETR4})
   * @param range history range to request from the underlying {@link MarketDataClient}
   * @param initialCash starting cash balance for the simulation
   * @param strategy trading strategy to evaluate
   * @return a {@link BacktestResult} containing summary performance metrics
   */
  public BacktestResult runSingleSymbol(
      String symbol, MarketDataRange range, BigDecimal initialCash, Strategy strategy) {
    List<Candle> daily = marketDataClient.getDailyCandles(symbol, range);

    if (daily.isEmpty()) {
      throw new IllegalArgumentException("No candles for symbol " + symbol);
    }

    SimulatedBroker broker = new SimulatedBroker(initialCash);
    int trades = 0;

    for (Candle c : daily) {
      AccountState state = broker.accountState();
      Position position = state.positions().get(symbol);
      BigDecimal posQty = position == null ? BigDecimal.ZERO : position.quantity();
      BigDecimal avgPrice = position == null ? BigDecimal.ZERO : position.averagePrice();

      StrategyContext ctx = new StrategyContext(state.cash(), posQty, avgPrice);
      MultiTimeframeSnapshot snapshot = new MultiTimeframeSnapshot(symbol, c, null, null);

      StrategyDecision decision = strategy.onBar(snapshot, ctx);
      if (decision == null || decision.type() == DecisionType.HOLD) {
        continue;
      }

      BigDecimal price = c.close();
      if (decision.type() == DecisionType.BUY) {
        BigDecimal toInvest =
            state
                .cash()
                .multiply(
                    BigDecimal.valueOf(
                        decision.positionSizePct() == null ? 1.0 : decision.positionSizePct()));
        if (toInvest.compareTo(BigDecimal.ZERO) <= 0) {
          continue;
        }
        BigDecimal qty = toInvest.divide(price, 6, BigDecimal.ROUND_HALF_UP);
        broker.applyTrade(symbol, price, qty);
        trades++;
      } else if (decision.type() == DecisionType.SELL && position != null) {
        BigDecimal qty =
            position
                .quantity()
                .multiply(
                    BigDecimal.valueOf(
                        decision.positionSizePct() == null ? 1.0 : decision.positionSizePct()));
        if (qty.compareTo(BigDecimal.ZERO) > 0) {
          broker.applyTrade(symbol, price, qty.negate());
          trades++;
        }
      }
    }

    AccountState finalState = broker.accountState();
    BigDecimal equity = finalState.cash();
    for (Map.Entry<String, Position> e : finalState.positions().entrySet()) {
      String sym = e.getKey();
      Position p = e.getValue();
      List<Candle> candles = marketDataClient.getDailyCandles(sym, range);
      if (!candles.isEmpty()) {
        BigDecimal lastPrice = candles.getLast().close();
        equity = equity.add(lastPrice.multiply(p.quantity()));
      }
    }

    return new BacktestResult(initialCash, finalState.cash(), equity, trades);
  }
}
