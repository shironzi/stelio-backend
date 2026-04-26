package com.aaronjosh.real_estate_app.dto.stats;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class StatsResDto {

    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal monthlyRevenueComparison;

    private Double occupancyRate;
    private Integer activeBookings;
    private Integer todaysCheckins;

    private List<PropertyResPerformanceDto> properties = new ArrayList<>();
}