package com.aaronjosh.real_estate_app.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingCardsResDto {

    // Booking fields
    private UUID id;
    private BookingStatus status;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer totalGuests;

    // Property fields
    private UUID propertyId;
    private String title;
    private BigDecimal price;
    private Integer totalBedroom;
    private String address;
    private String city;
    private String imageUrl;

    // User Info
    private String contactPhone;
}
