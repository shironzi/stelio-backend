package com.aaronjosh.real_estate_app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.dto.auth.UserDetails;
import com.aaronjosh.real_estate_app.dto.booking.PropertyBookingResDto;
import com.aaronjosh.real_estate_app.dto.property.PropertyDto;
import com.aaronjosh.real_estate_app.dto.property.PropertyResDto;
import com.aaronjosh.real_estate_app.dto.property.UpdatePropertyDto;
import com.aaronjosh.real_estate_app.models.UserEntity.Role;
import com.aaronjosh.real_estate_app.services.BookingService;
import com.aaronjosh.real_estate_app.services.PropertyService;
import com.aaronjosh.real_estate_app.services.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<?> getProperties() {
        boolean isOwner = false;

        // Checks if user is unauthenticated
        UserDetails user = userService.getUserDetails();
        if (user != null) {
            isOwner = user.getRole().equals(Role.OWNER);
        }

        List<PropertyResDto> properties = isOwner ? propertyService.getMyPropeties() : propertyService.getProperties();

        return ResponseEntity.ok(Map.of("success", true, "properties", properties));
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<?> getProperty(@Valid @PathVariable UUID propertyId) {
        PropertyResDto property = propertyService.getPropertyById(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "property", property));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{propertyId}/bookings")
    public ResponseEntity<?> getPropertyBookings(@Valid @PathVariable UUID propertyId) {
        List<PropertyBookingResDto> bookings = bookingService.getPropertyBookingsByPropertyId(propertyId);

        return ResponseEntity
                .ok(Map.of("success", true, "message", "successfully fetched the bookings", "bookings", bookings));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> addProperty(@ModelAttribute PropertyDto property) {
        propertyService.addProperty(property);

        return ResponseEntity.ok(Map.of("success", true, "message", "Property created Successfully"));
    }

    @PostMapping("/{propertyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> editProperty(@PathVariable UUID propertyId,
            @RequestBody UpdatePropertyDto propertyDto) {
        propertyService.editProperty(propertyDto, propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Property Updated Successfully"));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> deleteProperty(@PathVariable UUID propertyId) {
        propertyService.deleteProperty(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Property Deleted Successfully"));
    }
}
