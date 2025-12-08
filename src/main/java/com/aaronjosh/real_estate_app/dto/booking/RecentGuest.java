package com.aaronjosh.real_estate_app.dto.booking;

import java.util.UUID;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class RecentGuest {
    public enum RecentGuestStatus {
        COMPLETED,
        CANCELLED,
        NOSHOW,
        PENDING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private Enum<RecentGuestStatus> status;
    private Double totalStar;
}
