package com.aaronjosh.real_estate_app.scheduler;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aaronjosh.real_estate_app.repositories.BlacklistedTokensRepo;
import com.aaronjosh.real_estate_app.repositories.IdempotencyRepo;

@Component
public class CleanupScheduler {

    @Autowired
    private BlacklistedTokensRepo blacklistedTokensRepo;

    @Autowired
    private IdempotencyRepo idempotencyRepo;

    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanupExpiredIdempotency() {
        LocalDateTime now = LocalDateTime.now();
        idempotencyRepo.deleteByExpiresAtBefore(now);
    }

    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanupExpiredBlacklistedTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokensRepo.deleteByExpiresAtBefore(now);
        System.out.println("delete now");
    }
}
