package com.aaronjosh.real_estate_app.dto.property;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingDateRange {
    private LocalDateTime start;
    private LocalDateTime end;
}
