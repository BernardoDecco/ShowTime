package com.horadoshow.robot.app.cli;

import com.horadoshow.robot.domain.Candle;
import com.horadoshow.robot.marketdata.MarketDataClients;
import com.horadoshow.robot.marketdata.MarketDataRange;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "candles", description = "Download daily candles (OHLCV) as CSV.")
public final class CandlesCommand implements Runnable {
  private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

  @Option(names = {"-s", "--symbol"}, defaultValue = "PETR4", description = "Ticker symbol (default: ${DEFAULT-VALUE}).")
  String symbol;

  @Option(names = {"-r", "--range"}, defaultValue = "1y", description = "Range: 6mo, 1y, 2y, 5y, max (default: ${DEFAULT-VALUE}).")
  String range;

  @Option(names = {"-o", "--out"}, description = "Output CSV file path. If omitted, prints to stdout.")
  Path out;

  @Override
  public void run() {
    MarketDataRange r = MarketDataRange.parse(range);
    List<Candle> candles = MarketDataClients.brapi().getDailyCandles(symbol, r);

    if (out != null) {
      try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out, StandardCharsets.UTF_8))) {
        writeCsv(candles, w);
      } catch (Exception e) {
        throw new RuntimeException("Failed to write file: " + out, e);
      }
    } else {
      PrintWriter w = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
      writeCsv(candles, w);
    }
  }

  private static void writeCsv(List<Candle> candles, PrintWriter w) {
    w.println("date,open,high,low,close,volume,adjustedClose");
    for (Candle c : candles) {
      String date = c.time().atZone(ZoneOffset.UTC).toLocalDate().format(DATE);
      w.printf(
          "%s,%s,%s,%s,%s,%d,%s%n",
          date,
          c.open(),
          c.high(),
          c.low(),
          c.close(),
          c.volume(),
          c.adjustedClose()
      );
    }
    w.flush();
  }
}

