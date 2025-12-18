package com.aaronjosh.real_estate_app.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.booking.PropertyBookingResDto;
import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.ConversationEntity;
import com.aaronjosh.real_estate_app.models.MessageEntity;
import com.aaronjosh.real_estate_app.models.ParticipantEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;
import com.aaronjosh.real_estate_app.repositories.ConversationRepository;
import com.aaronjosh.real_estate_app.repositories.PropertyRepository;
import com.aaronjosh.real_estate_app.util.BookingMessageTemplate;
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

    @Autowired
    private BookingMessageTemplate bookingMessageTemplate;

    @Autowired
    private ConversationRepository conversationRepo;

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
    // Create booking request.
    public void requestBooking(UUID propertyId, BookingReqDto bookingInfo) {
        // Renter details
        UserEntity user = userService.getUserEntity();

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

        // Create booking entity
        BookingEntity booking = new BookingEntity();
        booking.setProperty(property);
        booking.setStatus(BookingStatus.PENDING);
        booking.setUser(user);
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

        // Create conversation
        ConversationEntity conversation = new ConversationEntity();

        // Create participants
        ParticipantEntity propertyOwner = new ParticipantEntity();
        propertyOwner.setWhoJoined(property.getHost());

        ParticipantEntity renter = new ParticipantEntity();
        renter.setWhoJoined(user);

        // Create initial message
        String messageTemplate = bookingMessageTemplate.MessageTemplate(bookingInfo, property);
        MessageEntity message = new MessageEntity();
        message.setMesssages(messageTemplate);
        message.setFrom(user);

        conversation.setParticipants(List.of(propertyOwner, renter));
        conversation.setMessages(List.of(message));
        propertyOwner.setConversation(conversation);
        renter.setConversation(conversation);

        conversationRepo.save(conversation);
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

    @Transactional
    public List<PropertyBookingResDto> getPropertyBookingsByPropertyId(UUID propertyId) {

        // checks the ownership of property
        UserEntity owner = userService.getUserEntity();
        PropertyEntity property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!owner.getId().equals(property.getHost().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<BookingEntity> bookings = property.getBookings();

        List<PropertyBookingResDto> dtos = new ArrayList<>();

        for (BookingEntity booking : bookings) {
            PropertyBookingResDto dto = new PropertyBookingResDto();

            String name = booking.getUser().getFirstname() + " " + booking.getUser().getLastname();
            long totalDays = ChronoUnit.DAYS.between(booking.getStartDateTime(), booking.getEndDateTime());
            Integer totalNights = (int) totalDays;

            dto.setTitle(property.getTitle());
            dto.setRenterName(name);
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
