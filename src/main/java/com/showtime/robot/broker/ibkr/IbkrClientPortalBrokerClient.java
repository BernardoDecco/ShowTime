package com.showtime.robot.broker.ibkr;

import com.showtime.robot.broker.BrokerClient;
import com.showtime.robot.broker.OrderRequest;
import com.showtime.robot.broker.OrderResponse;
import com.showtime.robot.broker.OrderStatus;
import java.time.Instant;
import java.util.Optional;

/**
 * Placeholder for a real EU-available broker integration.
 *
 * <p>Interactive Brokers is commonly used in the EU and provides APIs (Client Portal / FIX). This
 * class is intentionally not implemented yet, but the project is structured so you can add it
 * behind the {@link BrokerClient} interface without touching strategies.
 */
public final class IbkrClientPortalBrokerClient implements BrokerClient {
  @Override
  public OrderResponse placeOrder(OrderRequest request) {
    return new OrderResponse(
        "ibkr-not-implemented",
        OrderStatus.REJECTED,
        Instant.now(),
        Optional.of("IBKR adapter not implemented yet. Use PaperBrokerClient for now."));
  }

  @Override
  public OrderResponse cancelOrder(String orderId) {
    return new OrderResponse(
        "ibkr-not-implemented",
        OrderStatus.REJECTED,
        Instant.now(),
        Optional.of("IBKR adapter not implemented yet."));
  }
}
