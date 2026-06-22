package com.example.orders.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

  private final KafkaTemplate<String, OrderStatusEvent> kafkaTemplate;

  @Value("${kafka.topic.order-status-events}")
  private String topic;

  public void publish(OrderStatusEvent event) {
    kafkaTemplate.send(topic, event.payload().orderId().toString(), event);
  }
}
