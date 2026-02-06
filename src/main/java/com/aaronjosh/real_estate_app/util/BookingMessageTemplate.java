package com.aaronjosh.real_estate_app.util;

import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.models.PropertyEntity;

@Component
public class BookingMessageTemplate {

    public String MessageTemplate(BookingReqDto bookingInfo, PropertyEntity property) {

        String ownerName = property.getHost().getFirstname() + " " + property.getHost().getLastname();

        // Calculate total nights
        LocalDateTime start = bookingInfo.getStart();
        LocalDateTime end = bookingInfo.getEnd();
        long totalDays = ChronoUnit.DAYS.between(start, end);
        int totalNights = (int) totalDays;

        return "Hello " + ownerName + "\n" +
                "🏠 Property\n" +
                property.getTitle() + "\n\n" +
                "📅 Stay Details\n" +
                "Check-in: " + start + "\n" +
                "Check-out: " + end + "\n" +
                "Total Nights: " + totalNights + "\n" +
                "Guests: " + bookingInfo.getTotalGuests() + "\n\n" +
                "👤 Guest Information\n" +
                "Contact Phone: " + bookingInfo.getContactPhone() + "\n" +
                "Guest Names: " + bookingInfo.getGuestNames() + "\n\n" +
                "📝 Special Requests\n" +
                "💳 Payment Status\n" +
                "Pending";
    }
}
