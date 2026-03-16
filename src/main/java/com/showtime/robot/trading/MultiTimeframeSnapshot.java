package com.showtime.robot.trading;

import com.showtime.robot.domain.Candle;

public record MultiTimeframeSnapshot(String symbol, Candle daily, Candle weekly, Candle monthly) {}
