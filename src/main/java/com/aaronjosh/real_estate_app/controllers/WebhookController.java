package com.aaronjosh.real_estate_app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aaronjosh.real_estate_app.services.PaymentService;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/stripe")
    public ResponseEntity<?> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("TESTING");
        System.out.println("TESTING");
        System.out.println("TESTING");
        System.out.println("TESTING");
        System.out.println("TESTING");
        System.out.println("TESTING");
        System.out.println("TESTING");

        paymentService.updateBookingStatus(payload, sigHeader);
        return ResponseEntity.ok().build();

    }
}
