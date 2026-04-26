package com.aaronjosh.real_estate_app.dto.stats;

import java.util.UUID;

import lombok.Data;

@Data
public class PropertyResPerformanceDto {
    private UUID id;
    private String title;
    private String address;
    private String occupancyRate;
    private Integer totalBookings;
    private Double totalRevenue;
    private String imageUrl;

    public PropertyResPerformanceDto(UUID id, String title, String address, Long totalBookings, Double totalRevenue,
            Double occupancyRate, String imageUrl) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.totalBookings = totalBookings.intValue();
        this.totalRevenue = totalRevenue;
        this.occupancyRate = String.format("%.2f", occupancyRate);
        this.imageUrl = imageUrl;
    }
}