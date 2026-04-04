package com.aaronjosh.real_estate_app.dto.property;

import java.util.UUID;

import lombok.Data;

@Data
public class ImageDto {
    private UUID id;
    private String url;
}
