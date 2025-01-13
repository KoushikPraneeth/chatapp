package com.koushik.chatapp;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

public class TestHelper {

    public static class TestStompFrameHandler implements StompFrameHandler {

        private final CompletableFuture<Message> completableFuture;

        public TestStompFrameHandler(
            CompletableFuture<Message> completableFuture
        ) {
            this.completableFuture = completableFuture;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Message.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            completableFuture.complete((Message) payload);
        }
    }
}
