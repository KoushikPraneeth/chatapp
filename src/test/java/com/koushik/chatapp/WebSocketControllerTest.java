package com.koushik.chatapp;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketControllerTest {

    @Autowired
    private MappingJackson2MessageConverter messageConverter; // Add this

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    private WebSocketStompClient stompClient;
    private String websocketUrl;

    @BeforeEach
    public void setup() {
        websocketUrl = "ws://localhost:" + port + "/ws";

        // Setup WebSocket client
        List<Transport> transports = List.of(
            new WebSocketTransport(new StandardWebSocketClient())
        );
        this.stompClient = new WebSocketStompClient(
            new SockJsClient(transports)
        );
        // Use the configured converter
        this.stompClient.setMessageConverter(messageConverter);

        // Create test user
        User testUser = new User();
        testUser.setUsername("testuser1");
        testUser.setPassword("password123");
        userService.register(testUser);
    }

    @Test
    public void testWebSocketConnection() throws Exception {
        CompletableFuture<Message> completableFuture =
            new CompletableFuture<>();

        // Custom session handler
        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(
                StompSession session,
                StompHeaders connectedHeaders
            ) {
                // Subscribe to the public topic
                session.subscribe(
                    "/topic/public",
                    new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return Message.class;
                        }

                        @Override
                        public void handleFrame(
                            StompHeaders headers,
                            Object payload
                        ) {
                            completableFuture.complete((Message) payload);
                        }
                    }
                );

                // Send a test message
                Message testMessage = new Message(
                    "testuser1",
                    null,
                    "Test message"
                );
                session.send("/app/chat.public", testMessage);
            }

            @Override
            public void handleException(
                StompSession session,
                StompCommand command,
                StompHeaders headers,
                byte[] payload,
                Throwable exception
            ) {
                completableFuture.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(
                StompSession session,
                Throwable exception
            ) {
                completableFuture.completeExceptionally(exception);
            }
        };

        // Connect to WebSocket server
        stompClient.connect(websocketUrl, sessionHandler);

        // Wait for the message with a longer timeout
        Message receivedMessage = completableFuture.get(10, TimeUnit.SECONDS);

        // Assertions
        assertNotNull(receivedMessage);
        assertEquals("Test message", receivedMessage.getContent());
        assertEquals("testuser1", receivedMessage.getSender());
    }
}
