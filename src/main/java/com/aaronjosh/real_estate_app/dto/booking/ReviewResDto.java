package com.aaronjosh.real_estate_app.dto.booking;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResDto {
    private String from;
    private String message;
    private LocalDateTime date;
}
