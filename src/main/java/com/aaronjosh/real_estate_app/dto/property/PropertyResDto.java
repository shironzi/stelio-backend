package com.aaronjosh.real_estate_app.dto.property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyStatus;
import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyType;

import lombok.Data;

@Data
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
    private String City;
    private PropertyStatus status;
    private List<String> image;
    private Boolean isFavorite;
    private List<LocalDateTime> bookingStartDateTime;
    private List<LocalDateTime> bookingEndDateTime;
}
