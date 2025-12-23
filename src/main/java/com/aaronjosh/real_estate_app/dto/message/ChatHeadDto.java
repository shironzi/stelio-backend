package com.aaronjosh.real_estate_app.dto.message;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatHeadDto {
    private String chatName;
    private String messagePreview;
    private LocalDateTime date;
    private String profileLink;
}
