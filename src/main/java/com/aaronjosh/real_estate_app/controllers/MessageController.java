package com.aaronjosh.real_estate_app.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.dto.message.ChatHeadDto;
import com.aaronjosh.real_estate_app.services.MessageService;

@Controller
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/")
    public ResponseEntity<?> getChatHeads() {
        List<ChatHeadDto> chatHeads = messageService.getChatHeads();

        return ResponseEntity
                .ok(Map.of("success", true, "message", "Successfully retrived messages", "chatHeads", chatHeads));
    }
}
