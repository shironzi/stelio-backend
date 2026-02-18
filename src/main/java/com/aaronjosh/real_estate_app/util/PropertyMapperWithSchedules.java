package com.aaronjosh.real_estate_app.util;

import org.springframework.stereotype.Component;

import com.aaronjosh.real_estate_app.dto.property.DateRange;
import com.aaronjosh.real_estate_app.dto.property.PropertyResDto;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;

@Component
public class PropertyMapperWithSchedules extends PropertyMapper {
    // adding the schedules to the Property Mapper
    @Override
    public PropertyResDto toDto(PropertyEntity property) {
        // reusing the logic from toDto
        PropertyResDto dto = super.toDto(property);

        // Filter only APPROVED bookings and converts bookings into DateRange objects
        dto.setBookings(property.getBookings().stream()
                .filter(booking -> booking.getStatus().equals(BookingStatus.CONFIRMED))
                .map(booking -> new DateRange(booking.getStartDateTime(),
                        booking.getEndDateTime()))
                .toList());

        // Returns property dto with booking schedules
        return dto;
    }
}
