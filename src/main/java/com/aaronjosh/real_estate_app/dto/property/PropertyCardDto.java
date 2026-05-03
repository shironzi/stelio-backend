package com.aaronjosh.real_estate_app.dto.property;

import java.math.BigDecimal;
import java.util.UUID;

import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyType;

import lombok.Data;

@Data
public class PropertyCardDto {
    private UUID id;
    private String title;
    private String city;
    private String address;
    private String imageUrl;
    private String propertyType;
    private BigDecimal price;
    private Boolean isFavorite;

    public PropertyCardDto(UUID id, String title, String city, String address, BigDecimal price,
            PropertyType propertyType,
            String imageKey, Boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.city = city;
        this.address = address;
        this.price = price;
        this.imageUrl = imageKey;
        this.propertyType = propertyType.toString();
        this.isFavorite = isFavorite;
    }
}
