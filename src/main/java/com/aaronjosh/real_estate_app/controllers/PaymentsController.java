package com.aaronjosh.real_estate_app.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.services.IdempotencyService;
import com.aaronjosh.real_estate_app.services.PaymentService;
import com.stripe.exception.StripeException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Controller
@RequestMapping("/api/payments")
public class PaymentsController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private IdempotencyService idemptService;

    @PostMapping("/{bookingId}")
    public ResponseEntity<?> generateStripePaymentIntent(@Valid @PathVariable UUID bookingId,
            @RequestHeader("Idempotency-Key") @NotNull String idempotencyKey) throws StripeException {

        return idemptService.handle(idempotencyKey, () -> paymentService.generateStripePaymentIntent(bookingId));
    }
}
