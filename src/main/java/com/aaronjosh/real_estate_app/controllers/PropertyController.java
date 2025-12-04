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

import com.aaronjosh.real_estate_app.dto.property.PropertyDto;
import com.aaronjosh.real_estate_app.dto.property.PropertyResDto;
import com.aaronjosh.real_estate_app.dto.property.UpdatePropertyDto;
import com.aaronjosh.real_estate_app.services.PropertyService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/property")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @GetMapping("/")
    public ResponseEntity<?> getProperties() {
        List<PropertyResDto> properties = propertyService.getProperties();

        return ResponseEntity.ok(Map.of("success", true, "properties", properties));
    }

    @GetMapping("/my-properties")
    public ResponseEntity<?> getMyProperties() {
        List<PropertyResDto> properties = propertyService.getMyPropeties();

        return ResponseEntity.ok(Map.of("success", true, "properties", properties));
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<?> getProperty(@Valid @PathVariable UUID propertyId) {
        PropertyResDto property = propertyService.getPropertyById(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "property", property));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> addProperty(@ModelAttribute PropertyDto property) {
        propertyService.addProperty(property);

        return ResponseEntity.ok(Map.of("success", true, "message", "Property created Successfully"));
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> editProperty(@PathVariable UUID propertyId,
            @RequestBody UpdatePropertyDto propertyDto) {
        propertyService.editProperty(propertyDto, propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Property Updated Successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> deleteProperty(@PathVariable UUID propertyId) {
        propertyService.deleteProperty(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Property Deleted Successfully"));
    }

}
