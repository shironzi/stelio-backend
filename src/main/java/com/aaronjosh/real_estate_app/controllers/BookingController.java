package com.aaronjosh.real_estate_app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aaronjosh.real_estate_app.dto.booking.UpdateBookingStatusReq;
import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.dto.booking.BookingResDto;
import com.aaronjosh.real_estate_app.models.UserEntity.Role;
import com.aaronjosh.real_estate_app.services.BookingService;
import com.aaronjosh.real_estate_app.services.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    @PreAuthorize("hasAnyRole('RENTER','OWNER')")
    public ResponseEntity<?> getBookings() {
        boolean isOwner = userService.getUserDetails().getRole().equals(Role.OWNER);

        List<BookingResDto> bookings = isOwner
                ? bookingService.getPropertyBookings()
                : bookingService.getBookings();

        return ResponseEntity.ok(Map.of("success", true, "bookings", bookings));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@Valid @PathVariable UUID bookingId) {
        BookingResDto booking = bookingService.getBookingById(bookingId);

        return ResponseEntity.ok(Map.of("success", true, "bookings", booking));
    }

    @PreAuthorize("hasRole('RENTER')")
    @PostMapping("/{propertyId}/book")
    public ResponseEntity<?> book(@Valid @PathVariable UUID propertyId, @RequestBody BookingReqDto booking) {

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.book(propertyId, booking));
    }

    @PreAuthorize("hasRole('RENTER')")
    @PostMapping("/{propertyId}/reserve")
    public ResponseEntity<?> reserve(@Valid @PathVariable UUID propertyId, @RequestBody BookingReqDto booking) {

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.reserve(propertyId, booking));
    }

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{bookingId}")
    public ResponseEntity<?> updateBookingStatus(@Valid @PathVariable UUID bookingId,
            @RequestBody UpdateBookingStatusReq status) {
        bookingService.updateBookingStatus(bookingId, status.status());

        return ResponseEntity.ok(Map.of("success", true, "message", "Successfully updated the booking status."));
    }

    @PreAuthorize("hasRole('RENTER')")
    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@Valid @PathVariable UUID bookingId) {
        bookingService.cancelBooking(bookingId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Successfully cancel the booking of property."));
    }
}
