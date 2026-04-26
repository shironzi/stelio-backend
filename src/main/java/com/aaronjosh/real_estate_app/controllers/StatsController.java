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
@RequestMapping("/api/properties/stats")
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

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRevenue", stats.getTotalRevenue());
        metrics.put("monthlyRevenue", stats.getMonthlyRevenue());
        metrics.put("occupancyRate", stats.getOccupancyRate());
        metrics.put("activeBookings", stats.getActiveBookings());
        metrics.put("todaysCheckins", stats.getTodaysCheckins());

        Map<String, Object> response = new HashMap<>();
        response.put("metrics", metrics);
        response.put("properties", stats.getProperties());

        return ResponseEntity.ok(response);
    }
}
