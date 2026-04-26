package com.aaronjosh.real_estate_app.dto.stats;

import java.util.List;

import lombok.Data;

@Data
public class BookingStatsDto {
    private Integer upcomingCheckins;
    private String nextBooking;
    private Integer currentGuests;
    private Integer checkOutToday;
    private List<ActiveBookingsDto> activeBookings;
}
