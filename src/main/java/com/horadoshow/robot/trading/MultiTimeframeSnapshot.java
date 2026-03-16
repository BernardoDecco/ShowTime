package com.horadoshow.robot.trading;

import com.horadoshow.robot.domain.Candle;

public record MultiTimeframeSnapshot(
    String symbol,
    Candle daily,
    Candle weekly,
    Candle monthly
) {}

