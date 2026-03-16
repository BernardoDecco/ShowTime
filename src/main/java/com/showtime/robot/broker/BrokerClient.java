package com.showtime.robot.broker;

public interface BrokerClient {
  OrderResponse placeOrder(OrderRequest request);

  OrderResponse cancelOrder(String orderId);
}
