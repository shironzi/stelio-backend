package com.aaronjosh.real_estate_app.dto.message;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ChatHeadDto {
    private String chatName;
    private String messagePreview;
    private LocalDateTime date;
    private String profileLink;
    private UUID conversationId;
}
