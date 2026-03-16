package com.horadoshow.robot.broker.paper;

import com.horadoshow.robot.broker.BrokerClient;
import com.horadoshow.robot.broker.OrderRequest;
import com.horadoshow.robot.broker.OrderResponse;
import com.horadoshow.robot.broker.OrderStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Safe default broker: accepts orders but does not execute them.
 */
public final class PaperBrokerClient implements BrokerClient {
  private final Map<String, OrderRequest> orders = new ConcurrentHashMap<>();

  @Override
  public OrderResponse placeOrder(OrderRequest request) {
    String id = UUID.randomUUID().toString();
    orders.put(id, request);
    return new OrderResponse(id, OrderStatus.ACCEPTED, Instant.now(), null);
  }

  @Override
  public OrderResponse cancelOrder(String orderId) {
    if (orderId == null || orderId.isBlank()) {
      return new OrderResponse("", OrderStatus.REJECTED, Instant.now(), java.util.Optional.of("orderId is required"));
    }
    OrderRequest removed = orders.remove(orderId);
    if (removed == null) {
      return new OrderResponse(orderId, OrderStatus.REJECTED, Instant.now(), java.util.Optional.of("order not found"));
    }
    return new OrderResponse(orderId, OrderStatus.CANCELLED, Instant.now(), null);
  }
}

