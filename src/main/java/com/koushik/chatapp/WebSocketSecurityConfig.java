package com.koushik.chatapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpDestMatchers("/app/**").authenticated() // Secure WebSocket endpoints
                .simpSubscribeDestMatchers("/topic/public", "/user/queue/private").authenticated() // Secure subscriptions
                .anyMessage().authenticated(); // Authenticate all messages
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSocket connections
        return true;
    }
}