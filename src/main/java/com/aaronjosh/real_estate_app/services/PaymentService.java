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

        System.out.println(payment.getAmount());

        if (payment.getAmount() == null ||
                payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment amount must be greater than 0");
        }

        if (payment.getAmount().compareTo(booking.getBalance()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment exceeds remaining balance");
        }

        BigDecimal rentPrice = booking.getProperty().getPrice();

        // Apply 30% rule only on first payment
        if (booking.getPaymentStatus() == PaymentStatus.PENDING) {
            BigDecimal minPartial = rentPrice.multiply(BigDecimal.valueOf(0.3));
            if (payment.getAmount().compareTo(minPartial) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Initial payment must be at least 30%");
            }
        }

        BigDecimal newBalance = booking.getBalance().subtract(payment.getAmount());
        booking.setBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.PAID);
        } else {
            booking.setStatus(BookingStatus.PENDING_PAYMENT);
            booking.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        bookingRepo.save(booking);
    }
}
