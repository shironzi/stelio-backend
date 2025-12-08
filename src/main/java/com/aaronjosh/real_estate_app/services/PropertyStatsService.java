package com.aaronjosh.real_estate_app.services;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.dto.booking.PropertyStatsResDto;
import com.aaronjosh.real_estate_app.models.PropertyStats;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.PropertyStatsRepo;

@Service
public class PropertyStatsService {

    @Autowired
    private UserService userService;

    @Autowired
    private PropertyStatsRepo propertyStatsRepo;

    public PropertyStatsResDto dashboard(UUID propertyId) {
        UserEntity user = userService.getUserEntity();

        PropertyStats propertyStats = propertyStatsRepo.findByProperty_id(Objects.requireNonNull(propertyId));

        PropertyStatsResDto statsDto = new PropertyStatsResDto();

        statsDto.setName(user.getFirstname());

        // Key Metrics
        statsDto.setEarningsToday(propertyStats.getEarningsToday());
        statsDto.setUpcomingCheckIns(propertyStats.getUpcomingCheckIns());
        statsDto.setPendingReviews(propertyStats.getPendingReviews());

        // Data Analytics
        statsDto.setMonthlyEarnings(propertyStats.getMonthlyEarnings());
        statsDto.setOccupancyRate(propertyStats.getOccupancyRate());

        // Booking Sumarry
        statsDto.setPending(propertyStats.getPending());
        statsDto.setApproved(propertyStats.getApproved());
        statsDto.setDeclined(propertyStats.getDeclined());
        statsDto.setCancelled(propertyStats.getCancelled());
        statsDto.setUpcomingBookings(propertyStats.getUpcomingBookings());

        return statsDto;
    }
}
