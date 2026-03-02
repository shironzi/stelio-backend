package com.aaronjosh.real_estate_app.dto.booking;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class BookingReqDto {
    @Column(nullable = false)
    private LocalDateTime start;

    @Column(nullable = false)
    private LocalDateTime end;

    @Column(nullable = false)
    private String contactPhone;

    private List<String> guestNames;
}
