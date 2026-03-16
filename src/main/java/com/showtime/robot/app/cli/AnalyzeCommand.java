package com.showtime.robot.app.cli;

import com.showtime.robot.analysis.Indicators;
import com.showtime.robot.analysis.SupportResistance;
import com.showtime.robot.analysis.SupportResistance.Level;
import com.showtime.robot.domain.Candle;
import com.showtime.robot.marketdata.MarketDataClients;
import com.showtime.robot.marketdata.MarketDataRange;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "analyze",
    description = "Fetch candles and print a quick swing-trade analysis (indicators + S/R).")
public final class AnalyzeCommand implements Runnable {
  private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
  private static final DateTimeFormatter TS =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(SAO_PAULO);

  @Option(
      names = {"-s", "--symbol"},
      defaultValue = "PETR4",
      description = "Ticker symbol (default: ${DEFAULT-VALUE}).")
  String symbol;

  @Option(
      names = {"-r", "--range"},
      defaultValue = "1y",
      description = "Range: 6mo, 1y, 2y, 5y, max (default: ${DEFAULT-VALUE}).")
  String range;

  @Override
  public void run() {
    MarketDataRange r = MarketDataRange.parse(range);
    List<Candle> candles = MarketDataClients.brapi().getDailyCandles(symbol, r);
    Candle last = candles.getLast();

    OptionalDouble sma20 = Indicators.smaClose(candles, 20);
    OptionalDouble sma50 = Indicators.smaClose(candles, 50);
    OptionalDouble ema20 = Indicators.emaClose(candles, 20);
    OptionalDouble rsi14 = Indicators.rsiClose(candles, 14);

    SupportResistance.Levels levels = SupportResistance.detect(candles, 3, 0.006);

    System.out.println("Symbol: " + symbol.toUpperCase());
    System.out.println("Candles: " + candles.size() + " (range=" + r.apiValue() + ")");
    System.out.println(
        "Last candle: "
            + TS.format(last.time())
            + " O="
            + last.open()
            + " H="
            + last.high()
            + " L="
            + last.low()
            + " C="
            + last.close()
            + " V="
            + last.volume());

    System.out.println();
    System.out.println("Indicators (close):");
    System.out.println("  SMA20: " + fmt(sma20));
    System.out.println("  SMA50: " + fmt(sma50));
    System.out.println("  EMA20: " + fmt(ema20));
    System.out.println("  RSI14: " + fmt(rsi14));

    System.out.println();
    System.out.println("Top supports:");
    printTop(levels.supports(), 5);

    System.out.println();
    System.out.println("Top resistances:");
    printTop(levels.resistances(), 5);
  }

  private static String fmt(OptionalDouble v) {
    return v.isPresent() ? String.format(Locale.US, "%.4f", v.getAsDouble()) : "n/a";
  }

  private static void printTop(List<Level> levels, int max) {
    if (levels.isEmpty()) {
      System.out.println("  (none)");
      return;
    }
    for (int i = 0; i < Math.min(max, levels.size()); i++) {
      Level l = levels.get(i);
      System.out.println(
          "  "
              + l.price()
              + " (touches="
              + l.touches()
              + ", last="
              + TS.format(l.lastTouch())
              + ")");
    }
  }
}
