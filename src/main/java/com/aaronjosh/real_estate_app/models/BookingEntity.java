package com.aaronjosh.real_estate_app.models;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "bookings")
public class BookingEntity {
    public enum BookingStatus {
        CANCELLED, // User or system cancelled
        PENDING_PAYMENT, // Reservation created, awaiting full payment
        PENDING_APPROVAL, // Request-to-book submitted, awaiting host approval
        CONFIRMED, // Payment completed or host approved
        REJECTED, // Host rejected request
        NOSHOW, // Guest didn’t arrive
        COMPLETED // Stay completed
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
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private List<String> guestNames;
    private Integer totalGuests;
    private String contactPhone;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public BookingEntity() {
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyEntity property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING_APPROVAL;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
