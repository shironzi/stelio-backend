package com.aaronjosh.real_estate_app.dto.property;

import java.math.BigDecimal;
import java.util.UUID;

import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyType;

import lombok.Data;

@Data
public class PropertyCardDto {
    private UUID id;
    private String title;
    private String address;
    private String imageUrl;
    private String propertyType;
    private BigDecimal price;

    public PropertyCardDto(UUID id, String title, String address, BigDecimal price, PropertyType propertyType,
            String imageKey) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.imageUrl = imageKey;
        this.propertyType = propertyType.toString();
    }
}
