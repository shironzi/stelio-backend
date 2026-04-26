package com.aaronjosh.real_estate_app.dto.stats;

import lombok.Data;

@Data
public class PropertyResPerformanceDto {
    private String title;
    private String address;
    private String occupancyRate;
    private Integer totalBookings;
    private Double totalRevenue;

    public PropertyResPerformanceDto(String title, String address, Long totalBookings, Double totalRevenue,
            Double occupancyRate) {
        this.title = title;
        this.address = address;
        this.totalBookings = totalBookings.intValue();
        this.totalRevenue = totalRevenue;
        this.occupancyRate = String.format("%.2f", occupancyRate);
    }
}