package com.aaronjosh.real_estate_app.models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "blacklistedTokens")
@Data
public class BlacklistedTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime expiresAt;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    public BlacklistedTokens() {
    }

}
