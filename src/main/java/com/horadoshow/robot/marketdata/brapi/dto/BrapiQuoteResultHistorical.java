package com.horadoshow.robot.marketdata.brapi.dto;

public record BrapiQuoteResultHistorical(
    Long date,
    Double open,
    Double high,
    Double low,
    Double close,
    Long volume,
    Double adjustedClose
) {}

