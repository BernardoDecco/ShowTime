package com.showtime.robot.analysis;

import com.showtime.robot.domain.Candle;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Price-based indicator utilities calculated on {@link Candle} series.
 *
 * <p>This class focuses on simple, commonly used indicators for swing trading: SMA, EMA, and RSI of
 * the close price. All methods are defensive and will return {@link OptionalDouble#empty()} when
 * there is not enough data or invalid parameters are provided, instead of throwing.
 */
public final class Indicators {
  private Indicators() {}

  /**
   * Computes a simple moving average (SMA) of the candle {@code close} prices.
   *
   * <p>The SMA is calculated over the last {@code period} candles in the list. The list is assumed
   * to be ordered from oldest (index 0) to newest (last index).
   *
   * @param candles ordered list of candles (oldest first)
   * @param period number of candles used in the average; must be {@code > 0}
   * @return the SMA value, or {@link OptionalDouble#empty()} if {@code candles == null}, {@code
   *     period <= 0}, or there are fewer than {@code period} candles
   */
  public static OptionalDouble smaClose(List<Candle> candles, int period) {
    if (candles == null || candles.size() < period || period <= 0) return OptionalDouble.empty();
    double sum = 0.0;
    for (int i = candles.size() - period; i < candles.size(); i++) {
      sum += candles.get(i).close().doubleValue();
    }
    return OptionalDouble.of(sum / period);
  }

  /**
   * Computes an exponential moving average (EMA) of the candle {@code close} prices.
   *
   * <p>The EMA is initialized from the close price of the first candle in the lookback window and
   * then updated forward using the standard smoothing factor {@code k = 2 / (period + 1)}.
   *
   * @param candles ordered list of candles (oldest first)
   * @param period number of candles in the EMA window; must be {@code > 0}
   * @return the EMA value, or {@link OptionalDouble#empty()} if {@code candles == null}, {@code
   *     period <= 0}, or there are fewer than {@code period} candles
   */
  public static OptionalDouble emaClose(List<Candle> candles, int period) {
    if (candles == null || candles.size() < period || period <= 0) return OptionalDouble.empty();
    double k = 2.0 / (period + 1.0);
    double ema = candles.get(candles.size() - period).close().doubleValue();
    for (int i = candles.size() - period + 1; i < candles.size(); i++) {
      double c = candles.get(i).close().doubleValue();
      ema = (c * k) + (ema * (1.0 - k));
    }
    return OptionalDouble.of(ema);
  }

  /**
   * Computes a Relative Strength Index (RSI) on candle {@code close} prices.
   *
   * <p>The implementation uses the classic average gain / average loss formula. It looks at {@code
   * period} price changes (so it needs at least {@code period + 1} candles), computes average
   * up-moves and down-moves, and returns a value between 0 and 100 where high values (e.g. &gt; 70)
   * indicate strong recent upside momentum.
   *
   * @param candles ordered list of candles (oldest first)
   * @param period number of changes used to compute the RSI; must be {@code > 0}
   * @return the RSI value (0–100), or {@link OptionalDouble#empty()} if {@code candles == null},
   *     {@code period <= 0}, or there are fewer than {@code period + 1} candles
   */
  public static OptionalDouble rsiClose(List<Candle> candles, int period) {
    if (candles == null || candles.size() < period + 1 || period <= 0)
      return OptionalDouble.empty();

    double gain = 0.0;
    double loss = 0.0;
    for (int i = candles.size() - period; i < candles.size(); i++) {
      double change =
          candles.get(i).close().doubleValue() - candles.get(i - 1).close().doubleValue();
      if (change >= 0) gain += change;
      else loss -= change;
    }

    double avgGain = gain / period;
    double avgLoss = loss / period;
    if (avgLoss == 0.0) return OptionalDouble.of(100.0);

    double rs = avgGain / avgLoss;
    double rsi = 100.0 - (100.0 / (1.0 + rs));
    return OptionalDouble.of(rsi);
  }
}
