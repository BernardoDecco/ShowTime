package com.showtime.robot.trading.broker;

import java.math.BigDecimal;
import java.util.Map;

public record AccountState(BigDecimal cash, Map<String, Position> positions) {}
