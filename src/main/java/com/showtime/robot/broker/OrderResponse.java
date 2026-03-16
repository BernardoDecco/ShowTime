package com.showtime.robot.broker;

import java.time.Instant;
import java.util.Optional;

public record OrderResponse(
    String orderId, OrderStatus status, Instant createdAt, Optional<String> message) {
  public OrderResponse {
    message = message == null ? Optional.empty() : message;
  }
}
