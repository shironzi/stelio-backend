package com.aaronjosh.real_estate_app.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class BookingResDto {

    // Booking fields
    private UUID id;
    private String paymentStatus;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String specialRequest;
    private List<String> guestNames;
    private Integer totalGuests;
    private String contactPhone;
    private String status;

    // Property fields
    private UUID propertyId;
    private String title;
    private String description;
    private BigDecimal price;
    private String propertyType;
    private Integer maxGuest;
    private Integer totalBedroom;
    private Integer totalBed;
    private Integer totalBath;
    private String address;
    private String city;
    private List<String> images;
}
