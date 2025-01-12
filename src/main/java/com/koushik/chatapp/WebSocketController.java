package com.koushik.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message handleMessage(Message message) {
        return messageService.sendMessage(message);
    }
}