package com.aaronjosh.real_estate_app.services;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.payment.PaymentReqDto;
import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.models.BookingEntity.PaymentStatus;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;

@Service
public class PaymentService {

    @Autowired
    private BookingRepo bookingRepo;

    public void processPayment(UUID bookingId, PaymentReqDto payment) {
        BookingEntity booking = bookingRepo.findById(bookingId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking already paid");
        }

        BigDecimal rentPrice = booking.getProperty().getPrice();
        BigDecimal partialPayment = rentPrice.multiply(BigDecimal.valueOf(0.3));

        if (payment.getAmount().compareTo(partialPayment) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Partial payment must be at least 30% of rent price");
        }

        BookingStatus bookingStatus = BookingStatus.PENDING_APPROVAL;
        PaymentStatus paymentStatus = PaymentStatus.PARTIAL;

        if (payment.getAmount().compareTo(rentPrice) == 0) {
            bookingStatus = BookingStatus.CONFIRMED;
            paymentStatus = PaymentStatus.PAID;
        }
        booking.setStatus(bookingStatus);
        booking.setPaymentStatus(paymentStatus);

        bookingRepo.save(booking);
    }
}
