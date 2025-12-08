package com.aaronjosh.real_estate_app.dto.booking;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpcomingBooking {
    private String name;
    private LocalDateTime CheckInDate;
    private Integer duration;
}
