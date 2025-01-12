package com.koushik.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.sendMessage(message));
    }

    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String recipient) {
        return ResponseEntity.ok(messageService.getMessagesForRecipient(recipient));
    }
}
