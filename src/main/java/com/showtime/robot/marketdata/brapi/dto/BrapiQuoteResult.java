package com.showtime.robot.marketdata.brapi.dto;

import java.util.List;

public record BrapiQuoteResult(
    String symbol,
    String usedInterval,
    String usedRange,
    List<BrapiQuoteResultHistorical> historicalDataPrice) {}
