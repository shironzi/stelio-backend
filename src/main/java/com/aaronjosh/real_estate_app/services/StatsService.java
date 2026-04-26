package com.aaronjosh.real_estate_app.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.dto.booking.BookingCalendarResDto;
import com.aaronjosh.real_estate_app.dto.booking.PropertyStatsResDto;
import com.aaronjosh.real_estate_app.dto.stats.PropertyResPerformanceDto;
import com.aaronjosh.real_estate_app.dto.stats.StatsResDto;
import com.aaronjosh.real_estate_app.dto.user.UserDetails;
import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.PropertyStats;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;
import com.aaronjosh.real_estate_app.repositories.PropertyStatsRepo;

@Service
public class StatsService {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private PropertyStatsRepo propertyStatsRepo;

    public PropertyStatsResDto dashboard(UUID propertyId) {
        UserDetails user = userService.getUserDetails();

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

    // 1. get properties that belongs to user
    // 2. compute the total revenue
    // 3. compute Monthly Revenue
    // 4. comppute occupancy rate
    // 4. compute active bookings
    // 5. return the first 5 bookings

    public StatsResDto stats() {
        UserDetails userDetails = userService.getUserDetails();

        StatsResDto stats = new StatsResDto();

        LocalDateTime now = LocalDateTime.now();

        // Current Month
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth()).atTime(23, 59,
                59);

        // Last Month
        LocalDateTime startOfLastMonth = now.minusMonths(1).toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = now.minusMonths(1).toLocalDate()
                .withDayOfMonth(now.minusMonths(1).toLocalDate().lengthOfMonth()).atTime(23, 59, 59);

        // Total revenue
        stats.setTotalRevenue(bookingRepo.getTotalRevenue(userDetails.getId()));

        // Current Month Revenue
        BigDecimal currentMonthRevenue = bookingRepo.getMonthlyRevenue(userDetails.getId(), startOfMonth, endOfMonth);
        stats.setCurrentMonthRevenue(currentMonthRevenue);

        // Last Month Revenue
        BigDecimal lastMonthRevenue = bookingRepo.getMonthlyRevenue(userDetails.getId(), startOfLastMonth,
                endOfLastMonth);

        // Comparing the last month and current month
        BigDecimal revenueComparisonPercentage = BigDecimal.ZERO;

        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueComparisonPercentage = currentMonthRevenue.subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            if (currentMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
                revenueComparisonPercentage = BigDecimal.valueOf(100);
            } else {
                revenueComparisonPercentage = BigDecimal.ZERO;
            }
        }

        stats.setCurrentMonthRevenueVsLastMonth(revenueComparisonPercentage);

        // Occupancy Rate
        Integer monthlyBooked = bookingRepo.getMonthlyTotalBooked(userDetails.getId(), startOfMonth, now);
        long availableDays = ChronoUnit.DAYS.between(startOfMonth, now);
        double occupancyRate = availableDays > 0 ? (double) monthlyBooked / availableDays : 0.0;

        stats.setOccupancyRate(Double.valueOf(occupancyRate));

        // Active bookings
        stats.setActiveBookings(bookingRepo.getActiveBookings(userDetails.getId(), startOfMonth, endOfMonth));

        // Todays Checkins
        LocalDateTime startToday = now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth()).atTime(0, 0, 0);
        stats.setTodaysCheckins(bookingRepo.getTodaysCheckins(userDetails.getId(), startToday, endOfMonth));

        // Top 3 Properties
        List<PropertyResPerformanceDto> properties = new ArrayList<>();
        for (Object[] res : bookingRepo.getTopRevenueProperties(userDetails.getId())) {
            String title = (String) res[0];
            String address = (String) res[1];
            Long totalBookings = (Long) res[2];
            Double totalRevenue = (Double) res[3];
            Long totalNightBooked = (Long) res[4];
            LocalDateTime createdAt = (LocalDateTime) res[5];

            long totalAvailableDays = ChronoUnit.DAYS.between(createdAt, now);
            double propertyOccupancyRate = totalAvailableDays > 0 ? (double) totalNightBooked / totalAvailableDays
                    : 0.0;

            PropertyResPerformanceDto dto = new PropertyResPerformanceDto(title, address, totalBookings, totalRevenue,
                    propertyOccupancyRate);
            properties.add(dto);
        }

        stats.setProperties(properties);

        return stats;
    }
}
