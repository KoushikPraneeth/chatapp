package com.koushik.chatapp;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketController {
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message handleMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
}
