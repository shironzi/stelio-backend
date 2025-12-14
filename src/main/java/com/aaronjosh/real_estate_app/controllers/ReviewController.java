package com.aaronjosh.real_estate_app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.dto.booking.ReviewStatsResDto;
import com.aaronjosh.real_estate_app.services.ReviewService;

@Controller
@RequestMapping("/api/property/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PreAuthorize("OWNER")
    @GetMapping("/stats/{propertyId}")
    public ResponseEntity<?> getReviews(@PathVariable UUID propertyId) {

        List<ReviewStatsResDto> dtos = reviewService.getReviews(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "successfully retrieved reviews", "reviews", dtos));
    }
}
