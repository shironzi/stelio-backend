package com.aaronjosh.real_estate_app.services;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.booking.ScheduleReqDto;
import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;
import com.aaronjosh.real_estate_app.repositories.PropertyRepository;
import com.aaronjosh.real_estate_app.util.DateTimeUtils;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private PropertyRepository propertyRepo;

    // returns bookings from a user.
    public List<BookingEntity> getBookings() {
        UserEntity user = userService.getUserEntity();

        return bookingRepo.findByUser_id(user.getId());
    }

    public List<BookingEntity> getPropertyBookings() {
        UserEntity user = userService.getUserEntity();

        return bookingRepo.findByProperty_Host_Id(user.getId());
    }

    // returns booking info by ID.
    public BookingEntity getBookingById(UUID bookingId) {
        UserEntity user = userService.getUserEntity();

        return bookingRepo.findById(Objects.requireNonNull(bookingId))
                .filter(booking -> booking.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    @Transactional
    // creating a request for booking a property.
    public void requestBooking(UUID propertyId, ScheduleReqDto bookingSchedule) {
        // get user details
        UserEntity user = userService.getUserEntity();

        // get property
        PropertyEntity property = propertyRepo.findById(Objects.requireNonNull(propertyId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "property not found"));

        // verify if there was a active request
        boolean hasActiveBooking = bookingRepo.existsByUser_IdAndStatus(user.getId(), BookingStatus.PENDING);

        if (hasActiveBooking) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have a pending booking.");
        }

        // checks if there was conflict on schedules
        List<BookingEntity> existingBookings = bookingRepo.findOverlappingBookings(property.getId(),
                bookingSchedule.getEnd(), bookingSchedule.getStart());

        if (!existingBookings.isEmpty()) {
            String startFormatted = DateTimeUtils.formatLongDateTime(bookingSchedule.getStart());
            String endFormatted = DateTimeUtils.formatLongDateTime(bookingSchedule.getEnd());

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The property is not available from " + startFormatted + " to " + endFormatted);
        }

        // creating a booking entity
        BookingEntity booking = new BookingEntity();
        booking.setProperty(property);
        booking.setStatus(BookingStatus.PENDING);
        booking.setUser(user);
        booking.setStartDateTime(bookingSchedule.getStart());
        booking.setEndDateTime(bookingSchedule.getEnd());
        bookingRepo.save(booking);
    }

    // cancel booking from renters
    public void cancelBooking(UUID bookingId) {
        BookingEntity booking = bookingRepo.findById(Objects.requireNonNull(bookingId, "bookingId must not be null"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepo.save(booking);
    }

    public void updateBookingStatus(UUID bookingId, BookingStatus status) {
        BookingEntity booking = bookingRepo.findById(Objects.requireNonNull(bookingId, "bookingId must not be null"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));

        UserEntity user = userService.getUserEntity();
        PropertyEntity property = booking.getProperty();

        // Check ownership
        if (!property.getHost().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You don't own this property");
        }

        // Allow only pending bookings to be updated
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending bookings can be updated");
        }

        // Prevent duplicate status update
        if (booking.getStatus() == status) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking already has this status");
        }

        // checks if there was conflict on schedules
        List<BookingEntity> existingBookings = bookingRepo.findOverlappingBookings(property.getId(),
                booking.getEndDateTime(), booking.getStartDateTime());

        if (!existingBookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "There was already a scheduled");
        }

        booking.setStatus(status);
        bookingRepo.save(booking);
    }
}
