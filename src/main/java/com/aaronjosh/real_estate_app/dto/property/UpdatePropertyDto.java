package com.aaronjosh.real_estate_app.dto.property;

import java.math.BigDecimal;
import java.util.List;

import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePropertyDto {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "Property Type is required")
    private PropertyType propertyType;

    @Min(1)
    private Integer maxGuest;

    @Min(0)
    private Integer totalBedroom;

    @Min(0)
    private Integer totalBed;

    @Min(0)
    private Integer totalBath;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private List<@Valid ExistingImagesDto> existingImages;

    private List<@Valid NewImagesDto> newImages;
}
