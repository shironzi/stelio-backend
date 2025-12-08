package com.aaronjosh.real_estate_app.dto.booking;

import java.util.List;

import lombok.Data;

@Data
public class PropertyStatsResDto {
    // user name
    private String name;

    // Key Metrics
    private Double earningsToday;
    private Integer upcomingCheckIns;
    private Integer pendingReviews;

    // Data Analytics
    private Double monthlyEarnings;
    private Double occupancyRate;

    // booking summary
    private Integer pending;
    private Integer approved;
    private Integer declined;
    private Integer cancelled;

    // Recent Reviews
    private List<RecentGuest> recentReviews;

    // private Upcoming Bookings
    private List<UpcomingBooking> upcomingBookings;

}
