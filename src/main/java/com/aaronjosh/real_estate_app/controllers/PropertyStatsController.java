package com.aaronjosh.real_estate_app.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.dto.booking.PropertyStatsResDto;
import com.aaronjosh.real_estate_app.services.PropertyStatsService;

@Controller
@RequestMapping("/api/property/stats")
public class PropertyStatsController {

    @Autowired
    private PropertyStatsService propertyStatsService;

    @GetMapping("/{propertyId}")
    public ResponseEntity<?> dashboard(@PathVariable UUID propertyId) {
        PropertyStatsResDto stats = propertyStatsService.dashboard(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "success", "stats", stats));
    }
}
