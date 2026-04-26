package com.aaronjosh.real_estate_app.dto.stats;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

import lombok.Data;

@Data
public class ActiveBookingsDto {
    private String profileLink;
    private String name;
    private String propertyTitle;
    private String propertyAddress;
    private LocalDateTime checkInDateTime;
    private LocalDateTime checkOutDateTime;
    private Double price;
    private String status;

    public ActiveBookingsDto(String profileLink, String name, String propertyTitle,
            String propertyAddress, LocalDateTime checkInDateTime,
            LocalDateTime checkOutDateTime, Double price,
            String status) {
        this.profileLink = profileLink;
        this.name = name;
        this.propertyTitle = propertyTitle;
        this.propertyAddress = propertyAddress;
        this.checkInDateTime = checkInDateTime;
        this.checkOutDateTime = checkOutDateTime;
        this.price = price;
        this.status = status;
    }
}
