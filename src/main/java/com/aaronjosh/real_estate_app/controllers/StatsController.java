package com.aaronjosh.real_estate_app.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.dto.booking.BookingCalendarResDto;
import com.aaronjosh.real_estate_app.dto.booking.PropertyStatsResDto;
import com.aaronjosh.real_estate_app.dto.stats.StatsResDto;
import com.aaronjosh.real_estate_app.services.StatsService;

@Controller
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/{propertyId}")
    public ResponseEntity<?> dashboard(@PathVariable UUID propertyId) {
        PropertyStatsResDto stats = statsService.dashboard(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "success", "stats", stats));
    }

    @GetMapping("/calendar/{propertyId}")
    public ResponseEntity<?> getCalendar(@PathVariable UUID propertyId) {
        List<BookingCalendarResDto> calendar = statsService.getCalendar(propertyId);

        return ResponseEntity
                .ok(Map.of("success", true, "message", "successfully retrieved the calendar", "calendar", calendar));
    }

    @GetMapping
    public ResponseEntity<?> stats() {
        StatsResDto stats = statsService.stats();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", stats.getTotalRevenue());
        summary.put("monthlyRevenue", stats.getCurrentMonthRevenue());
        summary.put("monthlyRevenueComparison", stats.getCurrentMonthRevenueVsLastMonth());
        summary.put("occupancyRate", stats.getOccupancyRate());
        summary.put("activeBookings", stats.getActiveBookings());
        summary.put("todaysCheckins", stats.getTodaysCheckins());

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("properties", stats.getProperties());
        response.put("success", true);

        return ResponseEntity.ok(response);
    }
}
