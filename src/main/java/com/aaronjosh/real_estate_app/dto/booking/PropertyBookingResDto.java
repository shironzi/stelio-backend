package com.aaronjosh.real_estate_app.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.models.BookingEntity.PaymentStatus;
import com.aaronjosh.real_estate_app.models.BookingEntity.SpecialRequest;

import lombok.Data;

@Data
public class PropertyBookingResDto {
    private String title;
    private String renterName;
    private Integer totalNights;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private PaymentStatus paymentStatus;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private Integer totalGuest;
    private SpecialRequest specialRequest;
}
