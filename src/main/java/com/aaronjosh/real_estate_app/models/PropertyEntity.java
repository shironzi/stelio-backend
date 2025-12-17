package com.aaronjosh.real_estate_app.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name = "property")
@ToString(exclude = { "favorites", "bookings", "stats", "reviews", "conversations" })
public class PropertyEntity {
    public enum PropertyType {
        APARTMENT,
        HOUSE,
        VILLA,
        CABIN
    }

    public enum PropertyStatus {
        ACTIVE,
        INACTIVE,
        PENDING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Description is required")
    private String description;

    @Column(nullable = false)
    @Min(0)
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Property Type is required")
    private PropertyType propertyType;

    @Column(nullable = false)
    @Min(1)
    private Integer maxGuest;

    @Column(nullable = false)
    @Min(0)
    private Integer totalBedroom;

    @Column(nullable = false)
    @Min(0)
    private Integer totalBed;

    @Column(nullable = false)
    @Min(0)
    private Integer totalBath;

    @Column(nullable = false)
    @NotBlank(message = "Address is required")
    private String address;

    @Column(nullable = false)
    @NotBlank(message = "City is required")
    private String city;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @Enumerated(EnumType.STRING)
    private PropertyStatus status = PropertyStatus.ACTIVE;

    @OneToMany(mappedBy = "propertyEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> image = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "host_id")
    private UserEntity host;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FavoriteEntity> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookingEntity> bookings = new ArrayList<>();

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PropertyStats stats;

    @OneToMany(mappedBy = "property", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReviewEntity> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
    private List<ConversationEntity> conversations = new ArrayList<>();

    public PropertyEntity() {
    }

    public void addFavorite(FavoriteEntity favorite) {
        favorites.add(favorite);
        favorite.setProperty(this);
    }

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
