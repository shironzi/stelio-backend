package com.aaronjosh.real_estate_app.dto.message;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class MessageDto {
    private UUID userId;
    private String name;
    private String message;
    private String[] filePaths;
    private LocalDateTime timestamp;
}
