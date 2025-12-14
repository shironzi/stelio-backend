package com.aaronjosh.real_estate_app.dto.booking;

import java.util.List;

import lombok.Data;

@Data
public class ReviewStatsResDto {
    private String title;
    private String message;
    private Double stars;
    private List<ReviewResDto> reviews;
}
