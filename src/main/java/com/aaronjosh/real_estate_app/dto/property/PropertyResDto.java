package com.aaronjosh.real_estate_app.dto.property;

import java.math.BigDecimal;
import java.util.UUID;

import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyStatus;
import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropertyResDto {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private PropertyType propertyType;
    private Integer maxGuest;
    private Integer totalBedroom;
    private Integer totalBed;
    private Integer totalBath;
    private String address;
    private String city;
    private PropertyStatus status;
    private boolean isFavorite;
}
