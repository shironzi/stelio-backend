package com.aaronjosh.real_estate_app.dto.booking;

import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

public record UpdateBookingStatusReq(BookingStatus status) {
}
