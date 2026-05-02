package com.aaronjosh.real_estate_app.scheduler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aaronjosh.real_estate_app.dto.booking.BookingWebhookResDto;
import com.aaronjosh.real_estate_app.models.BookingEntity.BookingStatus;
import com.aaronjosh.real_estate_app.repositories.BlacklistedTokensRepo;
import com.aaronjosh.real_estate_app.repositories.BookingRepo;
import com.aaronjosh.real_estate_app.repositories.IdempotencyRepo;

import jakarta.transaction.Transactional;

@Component
public class Scheduler {

    @Autowired
    private BlacklistedTokensRepo blacklistedTokensRepo;

    @Autowired
    private IdempotencyRepo idempotencyRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Schedule tasks executes everyday at 2:30 AM
    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanupExpiredIdempotency() {
        LocalDateTime now = LocalDateTime.now();
        idempotencyRepo.deleteByExpiresAtBefore(now);
    }

    // Schedule tasks executes everyday at 2:30 AM
    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanupExpiredBlacklistedTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokensRepo.deleteByExpiresAtBefore(now);
    }

    @Transactional
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateBookingStatusToInprogress() {
        for (BookingWebhookResDto booking : bookingRepo.findBookingByStatusAndStartDateTime(BookingStatus.CONFIRMED,
                LocalDateTime.now())) {
            Map<String, Object> update = new HashMap<>();
            update.put("id", booking.getBookingId());
            update.put("status", booking.getBookingStatus().toString());
            messagingTemplate.convertAndSendToUser(booking.getUserId().toString(),
                    "/my-bookings", update);
        }
        bookingRepo.updateBookingStatusByStartDateTime(
                BookingStatus.INPROGRESS,
                BookingStatus.CONFIRMED,
                LocalDateTime.now());
    }

    @Transactional
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateBookingStatusCompleted() {
        for (BookingWebhookResDto booking : bookingRepo.findBookingByStatusAndEndDateTime(BookingStatus.INPROGRESS,
                LocalDateTime.now())) {
            Map<String, Object> update = new HashMap<>();
            update.put("id", booking.getBookingId());
            update.put("status", booking.getBookingStatus().toString());
            messagingTemplate.convertAndSendToUser(booking.getUserId().toString(),
                    "/my-bookings", update);
        }
        bookingRepo.updateBookingStatusByEndDateTime(
                BookingStatus.COMPLETED,
                BookingStatus.INPROGRESS,
                LocalDateTime.now());
    }
}
