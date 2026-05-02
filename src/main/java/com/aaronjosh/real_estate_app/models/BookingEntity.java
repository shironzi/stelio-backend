package com.aaronjosh.real_estate_app.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "bookings", indexes = {
        @Index(name = "idx_property", columnList = "property_id")
})

public class BookingEntity {
    public enum BookingStatus {
        CANCELLED, // User or system cancelled
        PENDING_PAYMENT, // Reservation created, awaiting full payment on lasts for 10 mins
        PENDING_APPROVAL, // Request-to-book submitted, awaiting host approval
        CONFIRMED, // Payment completed or host approved
        REJECTED, // Host rejected request
        NOSHOW, // Guest didn’t arrive
        INPROGRESS, // Guests are currently staying
        COMPLETED, // Stay completed
        EXPIRED // expired at 10 mins if not paid
    }

    public enum PaymentStatus {
        PENDING, // Payment not yet made
        PAID, // Full payment completed
        PARTIAL // Partial payment made (e.g., 30% reservation)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private BigDecimal balance;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private List<String> guestNames;
    private Integer totalGuests;
    private String contactPhone;
    private LocalDateTime expiresAt;

    private Double price;
    private Integer totalNights;

    @Column(unique = true)
    private String stripePaymentIntentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookingEntity() {
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyEntity property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BookingStatus status;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusMinutes(10);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
