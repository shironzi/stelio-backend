package com.aaronjosh.real_estate_app.dto.booking;

import java.util.UUID;

import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingWebhookResDto {
    private UUID bookingId;
    private UUID userId;
    private BookingStatus bookingStatus;
}
