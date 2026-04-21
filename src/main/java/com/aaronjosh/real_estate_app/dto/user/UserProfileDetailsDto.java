package com.aaronjosh.real_estate_app.dto.user;

import java.time.LocalDate;
import java.util.Map;

import lombok.Data;

@Data
public class UserProfileDetailsDto {
    // Personal Info
    private String firstname;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String city;
    private String profileLink;
    private LocalDate joinedAt;

    // Activities
    private Integer bookingsMade;
    private Integer nightStayed;
    private Integer upcomingStays;
    private Integer reviewsGiven;
    private Integer totalSpent;

    // // get personal info
    public Map<String, Object> getUserInfo() {
        return Map.of(
                "firstname", firstname,
                "lastName", lastName,
                "email", email,
                "phoneNumber", phoneNumber != null ? phoneNumber : "",
                "city", city != null ? city : "",
                "joinedAt", joinedAt,
                "profileLink", profileLink != null ? profileLink : "");
    }

    // // Get Activites
    public Map<String, Object> getActivities() {
        return Map.of(
                "bookingsMade", bookingsMade,
                "nightStayed", nightStayed,
                "upcomingStays", upcomingStays,
                "reviewsGiven", reviewsGiven,
                "totalSpent", totalSpent);
    }
}