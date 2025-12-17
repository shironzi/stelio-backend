package com.aaronjosh.real_estate_app.util;

import org.springframework.stereotype.Component;

import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.models.PropertyEntity;

@Component
public class BookingMessageTemplate {
    public String MessageTemplate(BookingReqDto bookingInfo, PropertyEntity property) {

        String ownerName = property.getHost().getFirstname() + " " + property.getHost().getLastname();

        return "Hello " + ownerName + "\n🏠 Property\n" + //
                "\n" + //
                property.getTitle() + //
                "\n" + //
                "\n" + //
                "📅 Stay Details\n" + //
                "\n" + //
                "Check-in: " + bookingInfo.getStart() + "\n" + //
                "\n" + //
                "Check-out: " + bookingInfo.getEnd() + "\n" + //
                "\n" + //
                "Total Nights: {totalNights}\n" + //
                "\n" + //
                "Guests: {totalGuests}\n" + //
                "\n" + //
                "👤 Guest Information\n" + //
                "\n" + //
                "Primary Guest: {primaryGuestName}\n" + //
                "\n" + //
                "Contact Phone: {contactPhone}\n" + //
                "\n" + //
                "Guest Names: {guestNames} (if provided)\n" + //
                "\n" + //
                "📝 Special Requests\n" + //
                "\n" + //
                "{specialRequests or \"None\"}\n" + //
                "\n" + //
                "💳 Payment Status\n" + //
                "\n" + //
                "{paymentStatus}";
    }
}
