package com.showtime.robot.marketdata.brapi.dto;

import java.util.List;

public record BrapiQuoteResponse(List<BrapiQuoteResult> results) {}
