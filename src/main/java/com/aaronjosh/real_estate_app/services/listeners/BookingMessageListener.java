package com.aaronjosh.real_estate_app.services.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.aaronjosh.real_estate_app.models.event.BookingRequestedEvent;
import com.aaronjosh.real_estate_app.services.MessageService;

public class BookingMessageListener {

    @Autowired
    private MessageService messageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingRequested(BookingRequestedEvent event) {
        // Triggered after a booking request transaction commits
        // Calls MessageService to create a conversation/message for the booking
        messageService.createBookingRequestMessage(
                event.getUser(), // the user who made the booking
                event.getBooking(), // booking request details
                event.getProperty() // property being booked
        );
    }

}
