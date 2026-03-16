package com.horadoshow.robot.analysis;

import com.horadoshow.robot.domain.Candle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Utilities to detect horizontal support and resistance levels from price history.
 *
 * <p>The algorithm first finds swing highs/lows (pivots) in the candle series and then
 * clusters them into price bands within a given tolerance. This is intended for
 * swing-trade style analysis and provides approximate zones, not exact tick levels.</p>
 */
public final class SupportResistance {
  /**
   * Logical type of a horizontal level: support (pivot lows) or resistance (pivot highs).
   */
  public enum LevelType { SUPPORT, RESISTANCE }

  /**
   * Describes a single detected level with basic statistics.
   *
   * @param type      whether this is a support or resistance level
   * @param price     representative price of the level (rounded to 2 decimals)
   * @param touches   how many pivots contributed to this level
   * @param lastTouch timestamp of the most recent pivot that touched this zone
   */
  public record Level(LevelType type, BigDecimal price, int touches, Instant lastTouch) {}

  /**
   * Container for detected support and resistance levels.
   *
   * @param supports    list of support levels
   * @param resistances list of resistance levels
   */
  public record Levels(List<Level> supports, List<Level> resistances) {}

  private SupportResistance() {}

  /**
   * Detects support and resistance levels from a series of candles.
   *
   * <p>Steps:
   * <ol>
   *   <li>Scan for local pivot highs and lows using {@code pivotLookback} candles on each side.</li>
   *   <li>Cluster nearby pivots into horizontal bands if their relative price difference
   *       is within {@code tolerancePct} (e.g. 0.005 for 0.5%).</li>
   *   <li>Return supports and resistances sorted by significance (touch count, recency, price).</li>
   * </ol>
   *
   * @param candles        ordered list of candles (oldest first); must not be {@code null}
   * @param pivotLookback  number of candles to look back/forward when deciding if a point
   *                       is a local high/low; must be {@code >= 1}
   * @param tolerancePct   maximum relative distance between prices to be merged into the
   *                       same level (e.g. 0.005 = 0.5%); must be {@code > 0}
   * @return a {@link Levels} object; if there are not enough candles to form pivots the
   *         lists will be empty
   * @throws NullPointerException     if {@code candles} is {@code null}
   * @throws IllegalArgumentException if {@code pivotLookback < 1} or {@code tolerancePct <= 0}
   */
  public static Levels detect(List<Candle> candles, int pivotLookback, double tolerancePct) {
    Objects.requireNonNull(candles, "candles");
    if (candles.size() < (pivotLookback * 2 + 1) || pivotLookback < 1) {
      return new Levels(List.of(), List.of());
    }
    if (tolerancePct <= 0.0) {
      throw new IllegalArgumentException("tolerancePct must be > 0 (example: 0.005 for 0.5%)");
    }

    List<Pivot> highs = new ArrayList<>();
    List<Pivot> lows = new ArrayList<>();

    for (int i = pivotLookback; i < candles.size() - pivotLookback; i++) {
      Candle c = candles.get(i);
      double hi = c.high().doubleValue();
      double lo = c.low().doubleValue();

      boolean pivotHigh = true;
      boolean pivotLow = true;

      for (int j = i - pivotLookback; j <= i + pivotLookback; j++) {
        if (j == i) continue;
        double otherHi = candles.get(j).high().doubleValue();
        double otherLo = candles.get(j).low().doubleValue();
        if (hi <= otherHi) pivotHigh = false;
        if (lo >= otherLo) pivotLow = false;
        if (!pivotHigh && !pivotLow) break;
      }

      if (pivotHigh) highs.add(new Pivot(LevelType.RESISTANCE, c.high(), c.time()));
      if (pivotLow) lows.add(new Pivot(LevelType.SUPPORT, c.low(), c.time()));
    }

    List<Level> supports = new ArrayList<>(cluster(lows, tolerancePct));
    List<Level> resistances = new ArrayList<>(cluster(highs, tolerancePct));

    supports.sort(levelSort());
    resistances.sort(levelSort());

    return new Levels(supports, resistances);
  }

  /**
   * Comparator used to order levels by significance and readability.
   *
   * <p>Order: more touches first, then most recent touch, then lower price.</p>
   */
  private static Comparator<Level> levelSort() {
    return Comparator
        .comparingInt(Level::touches).reversed()
        .thenComparing(Level::lastTouch, Comparator.reverseOrder())
        .thenComparing(Level::price);
  }

  /**
   * Internal pivot point representation used before clustering.
   */
  private record Pivot(LevelType type, BigDecimal price, Instant time) {}

  /**
   * Groups nearby pivot prices into horizontal levels using a relative tolerance.
   *
   * @param pivots       list of pivot highs or lows
   * @param tolerancePct maximum relative distance between prices to be merged
   * @return list of aggregated {@link Level} objects
   */
  private static List<Level> cluster(List<Pivot> pivots, double tolerancePct) {
    List<Cluster> clusters = new ArrayList<>();
    for (Pivot p : pivots) {
      Cluster best = null;
      double bestDistance = Double.POSITIVE_INFINITY;
      for (Cluster c : clusters) {
        double d = relDiff(p.price.doubleValue(), c.price.doubleValue());
        if (d <= tolerancePct && d < bestDistance) {
          best = c;
          bestDistance = d;
        }
      }
      if (best == null) {
        clusters.add(new Cluster(p.type, normalizePrice(p.price), 1, p.time));
      } else {
        best.touches += 1;
        best.lastTouch = max(best.lastTouch, p.time);
        best.price = normalizePrice(BigDecimal.valueOf((best.price.doubleValue() * 0.7) + (p.price.doubleValue() * 0.3)));
      }
    }

    return clusters.stream()
        .map(c -> new Level(c.type, c.price, c.touches, c.lastTouch))
        .toList();
  }

  /**
   * Normalizes price to 2 decimal places using {@link RoundingMode#HALF_UP},
   * to avoid noisy fractions in reported levels.
   */
  private static BigDecimal normalizePrice(BigDecimal v) {
    return v.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Relative absolute difference between two prices, used for clustering.
   *
   * @param a first price
   * @param b second price
   * @return {@code |a - b| / max(1e-9, mean(|a|, |b|))}
   */
  private static double relDiff(double a, double b) {
    double denom = Math.max(1e-9, (Math.abs(a) + Math.abs(b)) / 2.0);
    return Math.abs(a - b) / denom;
  }

  /**
   * Returns the latest of two instants.
   */
  private static Instant max(Instant a, Instant b) {
    return a.isAfter(b) ? a : b;
  }

  private static final class Cluster {
    final LevelType type;
    BigDecimal price;
    int touches;
    Instant lastTouch;

    private Cluster(LevelType type, BigDecimal price, int touches, Instant lastTouch) {
      this.type = type;
      this.price = price;
      this.touches = touches;
      this.lastTouch = lastTouch;
    }
  }
}

