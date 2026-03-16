package com.showtime.robot.trading.broker;

import java.math.BigDecimal;

public record Position(String symbol, BigDecimal quantity, BigDecimal averagePrice) {}
