package com.example.orders.order;

import com.example.orders.order.event.OrderEventConsumer;
import com.example.orders.order.event.OrderEventPayload;
import com.example.orders.order.event.OrderStatusEvent;
import com.example.orders.order.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    OrderEventConsumer consumer;

    @Test
    void consume_sendsEventToCorrectDestination() {
        UUID orderId = UUID.randomUUID();
        OrderStatusEvent event = new OrderStatusEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OrderStatus.CONFIRMED,
                Instant.now(),
                new OrderEventPayload(orderId, BigDecimal.TEN, null)
        );

        consumer.consume(event);

        verify(messagingTemplate).convertAndSend("/topic/orders/" + orderId, event);
    }
}
