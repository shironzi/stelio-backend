/**
 * User Entity
 * ---------------------------------
 * Represents user in the system.
 * 
 * - Contains personal details (fname, lname, email)
 * - Stores authentication data (Hashed password, role)
 * - Automatically update the creation and date timestampt
 */

package com.aaronjosh.real_estate_app.models;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name = "users")
@ToString(exclude = { "favorites", "properties", "bookings", "reviews", "joinedConversations", "messages" })
public class UserEntity {
    public enum Role {
        ADMIN,
        OWNER,
        RENTER,
    }

    // entity or table
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Firstname is required")
    @Column(nullable = false)
    private String firstname;

    @NotBlank(message = "Lastname is required")
    @Column(nullable = false)
    private String lastname;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.RENTER;

    @Column(nullable = false)
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_picture_id")
    private FileEntity profilePicture;

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FileEntity> uploads = new ArrayList<>();

    @OneToMany(mappedBy = "host", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PropertyEntity> properties;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FavoriteEntity> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<BookingEntity> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ReviewEntity> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "whoJoined", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ParticipantEntity> joinedConversations = new ArrayList<>();

    @OneToMany(mappedBy = "from", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MessageEntity> messages = new ArrayList<>();

    public UserEntity() {
    }

    public void addFavorite(FavoriteEntity favorite) {
        favorites.add(favorite);
        favorite.setUser(this);
    }

    public UserEntity(String firstName, String lastName, String email, Role role) {
        this.firstname = firstName;
        this.lastname = lastName;
        this.email = email;
        this.role = role;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /*
     * Sets created_at and updated_at before persisting.
     */

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    /*
     * Sets update_at before updating.
     */

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // returns the fullname.
    @Transient
    public String getFullName() {
        return (firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "");
    }
}
