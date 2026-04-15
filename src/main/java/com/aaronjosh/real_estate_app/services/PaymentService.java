package com.aaronjosh.real_estate_app.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.PaymentStatus;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.StripeException;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {

    @Autowired
    private BookingRepo bookingRepo;

    @Transactional
    public String generateStripePaymentIntent(UUID bookingId) throws StripeException {
        BookingEntity booking = bookingRepo.findById(bookingId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking already paid");
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(booking.getBalance()
                        .multiply(new BigDecimal("100"))
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValue())
                .setCurrency("php")
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        booking.setStripePaymentIntentId(paymentIntent.getId());
        bookingRepo.save(booking);

        return paymentIntent.getClientSecret();
    }
}
