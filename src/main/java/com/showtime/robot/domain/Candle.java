package com.showtime.robot.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Candle(
    Instant time,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    long volume,
    BigDecimal adjustedClose) {}
