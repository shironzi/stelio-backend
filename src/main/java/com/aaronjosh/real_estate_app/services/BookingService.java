package com.aaronjosh.real_estate_app.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.booking.PropertyBookingResDto;
import com.aaronjosh.real_estate_app.dto.auth.UserDetails;
import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.dto.booking.BookingResDto;
import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.event.BookingRequestedEvent;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;
import com.aaronjosh.real_estate_app.repositories.PropertyRepository;
import com.aaronjosh.real_estate_app.repositories.UserRepository;
import com.aaronjosh.real_estate_app.services.listeners.BookingMessageListener;
import com.aaronjosh.real_estate_app.util.DateTimeUtils;
import com.aaronjosh.real_estate_app.util.LinkGenerator;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PropertyRepository propertyRepo;

    @Autowired
    private BookingMessageListener eventPublisher;

    @Autowired
    private LinkGenerator linkGenerator;

    // returns bookings from a user.
    public List<BookingResDto> getBookings() {
        UserDetails user = userService.getUserDetails();

        List<BookingResDto> bookings = bookingRepo.findByUser_id(user.getId()).stream().map(
                (booking) -> {
                    BookingResDto dto = new BookingResDto();

                    // Booking fields
                    dto.setId(booking.getId());
                    dto.setPaymentStatus(booking.getPaymentStatus().toString());
                    dto.setStartDateTime(booking.getStartDateTime());
                    dto.setEndDateTime(booking.getEndDateTime());
                    dto.setSpecialRequest(booking.getSpecialRequest().toString());
                    dto.setGuestNames(booking.getGuestNames());
                    dto.setTotalGuests(booking.getTotalGuests());
                    dto.setContactPhone(booking.getContactPhone());
                    dto.setStatus(booking.getStatus().toString());

                    // Property fields
                    dto.setPropertyId(booking.getProperty().getId());
                    dto.setTitle(booking.getProperty().getTitle());
                    dto.setDescription(booking.getProperty().getDescription());
                    dto.setPrice(booking.getProperty().getPrice());
                    dto.setPropertyType(booking.getProperty().getPropertyType().toString());
                    dto.setMaxGuest(booking.getProperty().getMaxGuest());
                    dto.setTotalBedroom(booking.getProperty().getTotalBedroom());
                    dto.setTotalBed(booking.getProperty().getTotalBed());
                    dto.setTotalBath(booking.getProperty().getTotalBath());
                    dto.setAddress(booking.getProperty().getAddress());
                    dto.setCity(booking.getProperty().getCity());

                    dto.setImages(booking.getProperty().getImages().stream()
                            .map(image -> linkGenerator.generateLink(image))
                            .collect(Collectors.toList()));

                    return dto;
                }).toList();

        return bookings;
    }

    public List<BookingEntity> getPropertyBookings() {
        UserDetails user = userService.getUserDetails();

        return bookingRepo.findByProperty_Host_Id(user.getId());
    }

    // returns booking info by ID.
    public BookingResDto getBookingById(UUID bookingId) {
        BookingEntity booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        BookingResDto dto = new BookingResDto();

        // Booking fields
        dto.setId(booking.getId());
        dto.setPaymentStatus(booking.getPaymentStatus().toString());
        dto.setStartDateTime(booking.getStartDateTime());
        dto.setEndDateTime(booking.getEndDateTime());
        dto.setSpecialRequest(booking.getSpecialRequest().toString());
        dto.setGuestNames(booking.getGuestNames());
        dto.setTotalGuests(booking.getTotalGuests());
        dto.setContactPhone(booking.getContactPhone());
        dto.setStatus(booking.getStatus().toString());

        // Property fields
        dto.setPropertyId(booking.getProperty().getId());
        dto.setTitle(booking.getProperty().getTitle());
        dto.setDescription(booking.getProperty().getDescription());
        dto.setPrice(booking.getProperty().getPrice());
        dto.setPropertyType(booking.getProperty().getPropertyType().toString());
        dto.setMaxGuest(booking.getProperty().getMaxGuest());
        dto.setTotalBedroom(booking.getProperty().getTotalBedroom());
        dto.setTotalBed(booking.getProperty().getTotalBed());
        dto.setTotalBath(booking.getProperty().getTotalBath());
        dto.setAddress(booking.getProperty().getAddress());
        dto.setCity(booking.getProperty().getCity());

        return dto;
    }

    @Transactional
    // Create booking request.
    public void requestBooking(UUID propertyId, BookingReqDto bookingInfo) {
        UserDetails user = userService.getUserDetails();

        // Property details
        PropertyEntity property = propertyRepo.findById(Objects.requireNonNull(propertyId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "property not found"));

        // Checks pending booking
        boolean hasActiveBooking = bookingRepo.existsByUser_IdAndStatus(user.getId(), BookingStatus.PENDING);

        if (hasActiveBooking) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have a pending booking.");
        }

        // Check for booking date conflicts
        List<BookingEntity> existingBookings = bookingRepo.findOverlappingBookings(property.getId(),
                bookingInfo.getEnd(), bookingInfo.getStart());

        if (!existingBookings.isEmpty()) {
            String startFormatted = DateTimeUtils.formatLongDateTime(bookingInfo.getStart());
            String endFormatted = DateTimeUtils.formatLongDateTime(bookingInfo.getEnd());

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The property is not available from " + startFormatted + " to " + endFormatted);
        }

        UserEntity userEntity = userRepo.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        // Create booking entity
        BookingEntity booking = new BookingEntity();
        booking.setProperty(property);
        booking.setStatus(BookingStatus.PENDING);
        booking.setUser(userEntity);
        booking.setStartDateTime(bookingInfo.getStart());
        booking.setEndDateTime(bookingInfo.getEnd());
        booking.setTotalGuests(bookingInfo.getTotalGuests());
        booking.setContactPhone(bookingInfo.getContactPhone());

        // Checks special requests
        if (bookingInfo.getSpecialRequest() != null) {
            booking.setSpecialRequest(bookingInfo.getSpecialRequest());
        }

        if (bookingInfo.getGuestNames() != null) {
            booking.setGuestNames(bookingInfo.getGuestNames());
        }

        bookingRepo.save(booking);
        eventPublisher.handleBookingRequested(new BookingRequestedEvent(bookingInfo, userEntity, property));
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

        UserDetails user = userService.getUserDetails();
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

    @Transactional
    public List<PropertyBookingResDto> getPropertyBookingsByPropertyId(UUID propertyId) {

        // checks the ownership of property
        UserDetails user = userService.getUserDetails();
        PropertyEntity property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!user.getId().equals(property.getHost().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<BookingEntity> bookings = property.getBookings();

        List<PropertyBookingResDto> dtos = new ArrayList<>();

        for (BookingEntity booking : bookings) {
            PropertyBookingResDto dto = new PropertyBookingResDto();

            long totalDays = ChronoUnit.DAYS.between(booking.getStartDateTime(), booking.getEndDateTime());
            Integer totalNights = (int) totalDays;

            dto.setId(booking.getId());
            dto.setTitle(property.getTitle());
            dto.setRenterName(booking.getUser().getFullName());
            dto.setTotalNights(totalNights);
            dto.setStartDateTime(booking.getStartDateTime());
            dto.setEndDateTime(booking.getEndDateTime());
            dto.setPaymentStatus(booking.getPaymentStatus());
            dto.setTotalPrice(BigDecimal.valueOf(totalNights).multiply(property.getPrice()));
            dto.setStatus(booking.getStatus());
            dto.setTotalGuest(booking.getTotalGuests());
            dto.setSpecialRequest(booking.getSpecialRequest());

            dtos.add(dto);
        }

        return dtos;
    }
}
