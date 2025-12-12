package com.aaronjosh.real_estate_app.dto.booking;

import java.time.LocalDateTime;
import java.util.UUID;

import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

import lombok.Data;

@Data
public class BookingCalendarResDto {
    private UUID id;
    private LocalDateTime endDateTime;
    private LocalDateTime startDateTime;
    private BookingStatus status;
}
