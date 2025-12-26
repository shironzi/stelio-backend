package com.aaronjosh.real_estate_app.dto.booking;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResDto {
    private String from;
    private String message;
    private LocalDateTime date;

    public ReviewResDto(String from, String message, LocalDateTime date) {
        this.from = from;
        this.message = message;
        this.date = date;
    }
}
