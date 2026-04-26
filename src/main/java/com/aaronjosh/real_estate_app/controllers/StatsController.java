package com.aaronjosh.real_estate_app.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.dto.stats.StatsResDto;
import com.aaronjosh.real_estate_app.services.StatsService;

@Controller
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping
    public ResponseEntity<?> overview() {
        try {
            StatsResDto stats = statsService.overview();

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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching stats");
        }
    }
}
