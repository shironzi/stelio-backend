package com.aaronjosh.real_estate_app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aaronjosh.real_estate_app.dto.booking.PropertyBookingResDto;
import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.services.BookingService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/book")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PreAuthorize("RENTER")
    @GetMapping("/renter/")
    public ResponseEntity<?> getBookings() {
        List<BookingEntity> bookings = bookingService.getBookings();

        return ResponseEntity.ok(Map.of("success", true, "booking", bookings));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/owner/")
    public ResponseEntity<?> getPropertiesBookings() {
        List<BookingEntity> bookings = bookingService.getPropertyBookings();

        return ResponseEntity.ok(Map.of("success", true, "booking", bookings));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/owner/{propertyId}")
    public ResponseEntity<?> getPropertyBookingsByPropertyId(@Valid @PathVariable UUID propertyId) {
        List<PropertyBookingResDto> bookings = bookingService.getPropertyBookingsByPropertyId(propertyId);

        return ResponseEntity
                .ok(Map.of("success", true, "message", "successfully fetched the bookings", "bookings", bookings));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@Valid @PathVariable UUID bookingId) {
        BookingEntity booking = bookingService.getBookingById(bookingId);

        return ResponseEntity.ok(Map.of("success", true, "booking", booking));
    }

    @PreAuthorize("hasRole('RENTER')")
    @PostMapping("/{bookingId}")
    public ResponseEntity<?> requestBooking(@Valid @PathVariable UUID bookingId, @RequestBody BookingReqDto booking) {
        bookingService.requestBooking(bookingId, booking);

        return ResponseEntity.ok(Map.of("success", true, "message", "Successfully requested to book a property."));
    }

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@Valid @PathVariable UUID bookingId,
            @RequestBody BookingStatus status) {
        bookingService.updateBookingStatus(bookingId, status);

        return ResponseEntity.ok(Map.of("success", true, "message", "Successfully updated the booking status."));
    }

    @PreAuthorize("hasRole('RENTER')")
    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@Valid @PathVariable UUID bookingId) {
        bookingService.cancelBooking(bookingId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Successfully cancel the booking of property."));
    }
}
