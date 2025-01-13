package com.koushik.chatapp;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebSocketControllerTest {

    @Autowired
    private UserService userService;

    private WebSocketStompClient stompClient;
    private final String WEBSOCKET_URI = "ws://localhost:8080/ws";
    private final String WEBSOCKET_TOPIC = "/topic/public";

    @BeforeEach
    public void setup() {
        List<Transport> transports = List.of(
            new WebSocketTransport(new StandardWebSocketClient())
        );
        this.stompClient = new WebSocketStompClient(
            new SockJsClient(transports)
        );
        this.stompClient.setMessageConverter(
                new MappingJackson2MessageConverter()
            );
    }

    @Test
    public void testWebSocketConnection()
        throws InterruptedException, ExecutionException, TimeoutException {
        // Create test user
        User testUser = new User();
        testUser.setUsername("testuser1");
        testUser.setPassword("password123");
        userService.register(testUser);

        CompletableFuture<Message> completableFuture =
            new CompletableFuture<>();

        StompSession session = stompClient
            .connectAsync(WEBSOCKET_URI, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

        session.subscribe(
            WEBSOCKET_TOPIC,
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Message.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    completableFuture.complete((Message) payload);
                }
            }
        );

        Message testMessage = new Message("testuser1", null, "Test message");
        session.send("/app/chat.public", testMessage);

        Message receivedMessage = completableFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);
        assertEquals("Test message", receivedMessage.getContent());
        assertEquals("testuser1", receivedMessage.getSender());
    }
}
