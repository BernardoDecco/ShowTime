package com.horadoshow.robot.trading.app;

import com.horadoshow.robot.marketdata.MarketDataClient;
import com.horadoshow.robot.marketdata.MarketDataClients;
import com.horadoshow.robot.marketdata.MarketDataRange;
import com.horadoshow.robot.trading.backtest.BacktestEngine;
import com.horadoshow.robot.trading.backtest.BacktestResult;
import com.horadoshow.robot.trading.strategies.DailySmaCrossStrategy;
import java.math.BigDecimal;

/**
 * Command line entrypoint that runs a backtest for a single symbol over a given history range
 * using the {@link DailySmaCrossStrategy} and prints a short performance summary.
 */
public final class BacktestRunner {

  /**
   * Runs a backtest.
   *
   * @param args command line arguments: {@code <SYMBOL> <RANGE>} (for example {@code PETR4 2y})
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: BacktestRunner <SYMBOL> <RANGE>");
      System.err.println("Example: BacktestRunner PETR4 2y");
      System.exit(1);
    }

    String symbol = args[0];
    MarketDataRange range = MarketDataRange.parse(args[1]);

    MarketDataClient client = MarketDataClients.brapi();
    BacktestEngine engine = new BacktestEngine(client);

    DailySmaCrossStrategy strategy = new DailySmaCrossStrategy(10, 30);

    BigDecimal initialCash = BigDecimal.valueOf(10_000);

    BacktestResult result = engine.runSingleSymbol(symbol, range, initialCash, strategy);

    System.out.println("Symbol: " + symbol);
    System.out.println("Range: " + range.apiValue());
    System.out.println("Initial cash: " + result.initialCash());
    System.out.println("Final cash: " + result.finalCash());
    System.out.println("Equity: " + result.equity());
    System.out.println("Trades: " + result.trades());
    System.out.println("Total return %: " + result.totalReturnPct());
  }
}

