package com.aaronjosh.real_estate_app.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.dto.payment.PaymentReqDto;
import com.aaronjosh.real_estate_app.services.PaymentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/payments")
public class PaymentsController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/{bookingId}")
    public ResponseEntity<?> processPayment(@Valid @PathVariable UUID bookingId,
            @RequestBody PaymentReqDto paymentInfo) {
        paymentService.processPayment(bookingId, paymentInfo);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Payment Successfully received."));
    }
}
