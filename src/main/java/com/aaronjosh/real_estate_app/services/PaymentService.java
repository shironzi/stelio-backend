package com.aaronjosh.real_estate_app.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.booking.BookingResDto;
import com.aaronjosh.real_estate_app.models.BookingEntity;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.models.BookingEntity.PaymentStatus;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {

    @Value("${STRIPE_WEBHOOK_KEY}")
    private String stripeWebhookSecret;

    @Value("${CLOUDFLARE_R2_PUBLIC_URL}")
    private String publicUrl;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Map<String, Object> generateStripePaymentIntent(UUID bookingId) {
        try {
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

            return Map.of(
                    "success", true,
                    "message", "Successfully generated payment intent secret",
                    "paymentIntent", paymentIntent.getClientSecret());
        } catch (StripeException e) {
            throw new RuntimeException("Payment processing failed", e);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during generating payment intent operation", e);
        }
    }

    @Transactional
    public void updateBookingStatus(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

            if (!"payment_intent.succeeded".equals(event.getType())) {
                return;
            }

            PaymentIntent paymentIntent = event.getDataObjectDeserializer().getObject()
                    .map(obj -> (PaymentIntent) obj)
                    .orElseThrow(() -> new IllegalStateException("Failed to deserialize event"));
            String paymentIntentId = paymentIntent.getId();

            BookingEntity booking = bookingRepo
                    .findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Booking not found for paymentIntentId: " + paymentIntentId));

            if (!"succeeded".equals(paymentIntent.getStatus())) {
                return;
            }

            long amountReceived = paymentIntent.getAmountReceived();
            long expectedAmount = booking.getBalance()
                    .multiply(new BigDecimal("100"))
                    .longValue();

            if (amountReceived != expectedAmount) {
                throw new IllegalStateException("Amount mismatch for booking " + booking.getId());
            }

            booking.setBalance(BigDecimal.ZERO);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.PAID);

            bookingRepo.save(booking);

            BookingResDto dto = new BookingResDto();

            dto.setId(booking.getId());
            dto.setPaymentStatus(booking.getPaymentStatus().toString());
            dto.setStart(booking.getStartDateTime());
            dto.setEnd(booking.getEndDateTime());
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
                    .map(image -> publicUrl + "/" + image.getKey())
                    .collect(Collectors.toList()));

            Map<String, String> update = new HashMap<>();
            update.put("id", dto.getId().toString());
            update.put("status", dto.getStatus().toString());

            messagingTemplate.convertAndSendToUser(booking.getUser().getId().toString(), "/my-bookings", update);
        } catch (SignatureVerificationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
        } catch (Exception e) {
            throw e;
        }
    }
}
