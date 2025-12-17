package com.aaronjosh.real_estate_app.dto.booking;

import java.time.LocalDateTime;
import java.util.List;

import com.aaronjosh.real_estate_app.models.BookingEntity.SpecialRequest;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class BookingReqDto {
    @Column(nullable = false)
    private LocalDateTime start;

    @Column(nullable = false)
    private LocalDateTime end;

    @Column(nullable = false)
    private Integer totalGuests;

    @Column(nullable = false)
    private String contactPhone;

    private SpecialRequest specialRequest;

    private List<String> guestNames;
}
