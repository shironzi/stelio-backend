package com.aaronjosh.real_estate_app.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aaronjosh.real_estate_app.dto.booking.PropertyBookingResDto;
import com.aaronjosh.real_estate_app.dto.property.PropertyDto;
import com.aaronjosh.real_estate_app.dto.property.UpdatePropertyDto;
import com.aaronjosh.real_estate_app.services.BookingService;
import com.aaronjosh.real_estate_app.services.PropertyService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/properties/")
    public ResponseEntity<?> getProperties(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String address,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime checkIn,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime checkOut,
            @RequestParam(required = false) Integer minGuests,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minPrice) {

        return ResponseEntity
                .ok(propertyService.getProperties(page, address, checkIn, checkOut, minGuests, maxPrice, minPrice));
    }

    @GetMapping("/owner/properties")
    public ResponseEntity<?> getMyproperties(@RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok().body(propertyService.getMyPropeties(page));
    }

    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<?> getProperty(@Valid @PathVariable UUID propertyId) {
        return ResponseEntity.ok(propertyService.getPropertyById(propertyId));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/properties/{propertyId}/bookings")
    public ResponseEntity<?> getPropertyBookings(@Valid @PathVariable UUID propertyId) {
        List<PropertyBookingResDto> bookings = bookingService.getPropertyBookingsByPropertyId(propertyId);

        return ResponseEntity
                .ok(Map.of("success", true, "message", "successfully fetched the bookings", "bookings", bookings));
    }

    @PostMapping(value = "/properties", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> addProperty(@ModelAttribute PropertyDto property) {
        propertyService.addProperty(property);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Property created Successfully"));
    }

    @PostMapping(value = "/properties/{propertyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> editProperty(@PathVariable UUID propertyId,
            @ModelAttribute UpdatePropertyDto propertyDto) {
        propertyService.editProperty(propertyDto, propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Property Updated Successfully"));
    }

    @DeleteMapping("/properties/{propertyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> deleteProperty(@PathVariable UUID propertyId) {
        propertyService.deleteProperty(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Property Deleted Successfully"));
    }
}
