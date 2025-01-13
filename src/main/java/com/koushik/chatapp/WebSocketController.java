package com.koushik.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat.public")
    @SendTo("/topic/public")
    public Message handlePublicMessage(@Payload Message message) {
        Message savedMessage = messageService.sendMessage(message);
        return savedMessage; // Return the saved message with timestamp
    }

    @MessageMapping("/chat.private")
    public Message handlePrivateMessage(@Payload Message message) {
        Message savedMessage = messageService.sendMessage(message);
        messagingTemplate.convertAndSendToUser(
            message.getRecipient(),
            "/queue/private",
            savedMessage
        );
        return savedMessage;
    }
}
