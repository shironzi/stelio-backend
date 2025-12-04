package com.aaronjosh.real_estate_app.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

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

        // List to store start and end datetimes of approved bookings
        List<LocalDateTime> schedulesStartDateTime = new ArrayList<>();
        List<LocalDateTime> schedulesEndDateTime = new ArrayList<>();

        // Filter only approved bookings and add their start and end date times to the
        // list
        property.getBookings().stream().filter((booking) -> booking.getStatus().equals(BookingStatus.APPROVED))
                .forEach((booking) -> {
                    schedulesStartDateTime.add(booking.getStartDateTime());
                    schedulesEndDateTime.add(booking.getEndDateTime());
                });

        // Set the collection booking schedules into dto
        dto.setBookingStartDateTime(schedulesStartDateTime);
        dto.setBookingEndDateTime(schedulesEndDateTime);

        // Returns the dto with the property info and booking schedules
        return dto;
    }
}
