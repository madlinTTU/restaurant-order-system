package com.example.orders.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "${kafka.topic.order-status-events}")
    public void consume(OrderStatusEvent event) {
        String destination = "/topic/orders/" + event.payload().orderId();
        messagingTemplate.convertAndSend(destination, event);
    }
}
