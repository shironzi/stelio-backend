package com.aaronjosh.real_estate_app.models.event;

import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;

import lombok.Data;

@Data
public class BookingRequestedEvent {

    private final BookingReqDto booking;
    private final UserEntity user;
    private final PropertyEntity property;

    public BookingRequestedEvent(BookingReqDto booking, UserEntity user, PropertyEntity property) {
        this.booking = booking;
        this.user = user;
        this.property = property;
    }
}
