package com.aaronjosh.real_estate_app.dto.message;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageResDto {
    private LocalDateTime time;
    private String message;
    private String from;

    public MessageResDto(String from, String message, LocalDateTime time) {
        this.from = from;
        this.message = message;
        this.time = time;
    }
}
