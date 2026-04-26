package com.aaronjosh.real_estate_app.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.dto.stats.ActiveBookingsDto;
import com.aaronjosh.real_estate_app.dto.stats.BookingStatsDto;
import com.aaronjosh.real_estate_app.dto.stats.PropertyResPerformanceDto;
import com.aaronjosh.real_estate_app.dto.stats.StatsResDto;
import com.aaronjosh.real_estate_app.dto.user.UserDetails;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;

@Service
public class StatsService {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepo bookingRepo;

    @Value("${CLOUDFLARE_R2_PUBLIC_URL}")
    private String publicUrl;

    public StatsResDto overview() {
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
        LocalDateTime startToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endToday = now.toLocalDate().atTime(23, 59, 59);
        stats.setTodaysCheckins(bookingRepo.getTodaysCheckins(userDetails.getId(), startToday, endToday));

        // Top 3 Properties
        List<PropertyResPerformanceDto> properties = new ArrayList<>();
        for (Object[] res : bookingRepo.findTopRevenueProperties(userDetails.getId())) {
            UUID propertyId = (UUID) res[0];
            String title = (String) res[1];
            String address = (String) res[2];
            Long totalBookings = (Long) res[3];
            Double totalRevenue = (Double) res[4];
            Long totalNightBooked = (Long) res[5];
            LocalDateTime createdAt = (LocalDateTime) res[6];
            String imageUrl = publicUrl + "/" + res[7];

            long totalAvailableDays = ChronoUnit.DAYS.between(createdAt, now);
            double propertyOccupancyRate = totalAvailableDays > 0 ? (double) totalNightBooked / totalAvailableDays
                    : 0.0;

            PropertyResPerformanceDto dto = new PropertyResPerformanceDto(propertyId, title, address, totalBookings,
                    totalRevenue, propertyOccupancyRate, imageUrl);

            properties.add(dto);
        }

        stats.setProperties(properties);

        return stats;
    }

    public BookingStatsDto bookingStats() {
        UserDetails userDetails = userService.getUserDetails();

        BookingStatsDto dto = new BookingStatsDto();

        LocalDateTime now = LocalDateTime.now();

        // Upcoming Bookings
        Integer upcomingBookings = bookingRepo.countUpcomingBookings(userDetails.getId(), now);
        dto.setUpcomingCheckins(upcomingBookings);

        // Next Booking
        LocalDateTime nextBooking = bookingRepo.findNextBooking(userDetails.getId(), now);
        Duration duration = Duration.between(now, nextBooking);

        if (duration.toDays() >= 1) {
            long days = duration.toDays();
            long remainingHours = duration.minusDays(days).toHours();
            dto.setNextBooking(days + " Days and " + remainingHours + " hours");
        } else {
            long hours = duration.toHours();
            dto.setNextBooking(hours + " hours");
        }

        // Current Guests
        Integer currentGuests = bookingRepo.countCurrentGuests(userDetails.getId());
        dto.setCurrentGuests(currentGuests);

        // Checkout Today
        LocalDateTime startToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endToday = now.toLocalDate().atTime(23, 59, 59);

        Integer checkOutToday = bookingRepo.countCheckOutToday(userDetails.getId(), startToday, endToday);
        dto.setCheckOutToday(checkOutToday);

        List<ActiveBookingsDto> activeBookings = new ArrayList<>();

        for (Object[] obj : bookingRepo.findActiveBookings(userDetails.getId(), now)) {
            String profilePictureKey = (String) obj[0];
            String firstname = (String) obj[1];
            String lastname = (String) obj[2];
            String propertyTitle = (String) obj[3];
            String propertyAddress = (String) obj[4];
            LocalDateTime startDateTime = (LocalDateTime) obj[5];
            LocalDateTime endDateTime = (LocalDateTime) obj[6];
            Double price = (Double) obj[7];
            String status = (String) obj[8];

            ActiveBookingsDto booking = new ActiveBookingsDto(
                    publicUrl + "/" + profilePictureKey,
                    firstname + " " + lastname,
                    propertyTitle,
                    propertyAddress,
                    startDateTime,
                    endDateTime,
                    price,
                    status);

            activeBookings.add(booking);
        }

        dto.setActiveBookings(activeBookings);

        return dto;
    }
}
