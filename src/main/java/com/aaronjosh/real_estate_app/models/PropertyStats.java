package com.aaronjosh.real_estate_app.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.aaronjosh.real_estate_app.dto.booking.RecentGuest;
import com.aaronjosh.real_estate_app.dto.booking.UpcomingBooking;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "propertyStats")
public class PropertyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Key Metrics
    private Double earningsToday = 0.0;
    private Integer upcomingCheckIns = 0;
    private Integer pendingReviews = 0;

    // Data Analytics
    private Double monthlyEarnings = 0.0;
    private Double occupancyRate = 0.0;

    // booking summary
    private Integer pending = 0;
    private Integer approved = 0;
    private Integer declined = 0;
    private Integer cancelled = 0;

    @OneToOne
    @JoinColumn(name = "property_id")
    private PropertyEntity property;

    @Transient
    private List<UpcomingBooking> upcomingBookings = new ArrayList<>();

    @Transient
    private List<RecentGuest> recentGuests = new ArrayList<>();
}
