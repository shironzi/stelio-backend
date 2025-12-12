package com.aaronjosh.real_estate_app.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.dto.booking.BookingCalendarResDto;
import com.aaronjosh.real_estate_app.dto.booking.PropertyStatsResDto;
import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.PropertyStats;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;
import com.aaronjosh.real_estate_app.repositories.PropertyStatsRepo;

@Service
public class PropertyStatsService {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private PropertyStatsRepo propertyStatsRepo;

    public PropertyStatsResDto dashboard(UUID propertyId) {
        UserEntity user = userService.getUserEntity();

        PropertyStats propertyStats = propertyStatsRepo.findByProperty_id(Objects.requireNonNull(propertyId));

        PropertyStatsResDto statsDto = new PropertyStatsResDto();

        statsDto.setName(user.getFirstname());

        // Key Metrics
        statsDto.setEarningsToday(propertyStats.getEarningsToday());
        statsDto.setUpcomingCheckIns(propertyStats.getUpcomingCheckIns());
        statsDto.setPendingReviews(propertyStats.getPendingReviews());

        // Data Analytics
        statsDto.setMonthlyEarnings(propertyStats.getMonthlyEarnings());
        statsDto.setOccupancyRate(propertyStats.getOccupancyRate());

        // Booking Sumarry
        statsDto.setPending(propertyStats.getPending());
        statsDto.setApproved(propertyStats.getApproved());
        statsDto.setDeclined(propertyStats.getDeclined());
        statsDto.setCancelled(propertyStats.getCancelled());
        statsDto.setUpcomingBookings(propertyStats.getUpcomingBookings());

        return statsDto;
    }

    public List<BookingCalendarResDto> getCalendar(UUID propertyId) {
        List<BookingCalendarResDto> bookingList = new ArrayList<>();

        List<BookingEntity> bookings = bookingRepo.findAllByPropertyId(propertyId);

        LocalDateTime date = LocalDateTime.now();

        for (BookingEntity booking : bookings) {
            // only show pending and approved bookings
            if (booking.getStatus() == BookingStatus.REJECTED
                    || booking.getStatus() == BookingStatus.CANCELLED)
                continue;

            // Skip bookings that already ended
            if (booking.getStartDateTime().isBefore(date) && booking.getEndDateTime().isBefore(date))
                continue;

            BookingCalendarResDto dto = new BookingCalendarResDto();
            dto.setId(booking.getId());
            dto.setEndDateTime(booking.getEndDateTime());
            dto.setStartDateTime(booking.getStartDateTime());
            dto.setStatus(booking.getStatus());

            bookingList.add(dto);
        }

        return bookingList;
    }
}
