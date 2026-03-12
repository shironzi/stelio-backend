package com.aaronjosh.real_estate_app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.dto.message.ChatHeadDto;
import com.aaronjosh.real_estate_app.dto.message.ConversationReqDto;
import com.aaronjosh.real_estate_app.dto.message.MessageDto;
import com.aaronjosh.real_estate_app.dto.message.MessageResDto;
import com.aaronjosh.real_estate_app.dto.message.SendMessageDto;
import com.aaronjosh.real_estate_app.services.MessageService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/")
    public ResponseEntity<?> getChatHeads() {
        List<ChatHeadDto> chatHeads = messageService.getChatHeads();

        return ResponseEntity
                .ok(Map.of("success", true, "message", "Successfully retrived messages", "chats", chatHeads));
    }

    @PostMapping("/")
    public ResponseEntity<?> createNewConversation(@RequestBody ConversationReqDto particiant) {
        messageService.createNewConversation(particiant);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Successfully created conversation"));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getMessageById(@Valid @PathVariable UUID conversationId) {

        List<MessageResDto> messages = messageService.getMessagesFromConversationId(conversationId);

        return ResponseEntity.ok(Map.of("success", true, "messages", messages));
    }

    @PostMapping("/{conversationId}")
    public ResponseEntity<?> sendMessage(@Valid @PathVariable UUID conversationId,
            @RequestBody SendMessageDto messageInfo) {

        MessageDto message = messageService.sendMessageByConversationId(conversationId, messageInfo);

        return ResponseEntity.ok(Map.of("success", true, "message", message));
    }
}
