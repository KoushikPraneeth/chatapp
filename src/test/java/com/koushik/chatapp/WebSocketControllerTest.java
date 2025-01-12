package com.koushik.chatapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebSocketControllerTest {

    @Test
    public void testWebSocket() throws Exception {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransport()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = stompClient.connect("ws://localhost:8080/ws", new StompSessionHandlerAdapter() {
        }).get(1, TimeUnit.SECONDS);

        session.subscribe("/topic/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.offer((String) payload);
            }
        });

        String message = "{\"content\":\"Test message\"}";
        session.send("/app/chat", message);

        String received = blockingQueue.poll(1, TimeUnit.SECONDS);
        assertEquals(message, received);
    }

    private List<Transport> createTransport() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }
}
