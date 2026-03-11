package com.aaronjosh.real_estate_app.dto.message;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class MessageResDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String message;
    private String[] filePaths;
    private LocalDateTime timestamp;

    public MessageResDto(UUID id, UUID userId, String name, String message, String[] filePaths,
            LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.message = message;
        this.filePaths = filePaths;
        this.timestamp = timestamp;
    }
}
