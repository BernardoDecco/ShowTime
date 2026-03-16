package com.showtime.robot.broker;

import java.math.BigDecimal;

public record Bracket(BigDecimal stopLossPrice, BigDecimal takeProfitPrice) {}
