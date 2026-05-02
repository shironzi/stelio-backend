package com.aaronjosh.real_estate_app.dto.property;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropertyImageDto {
    private UUID id;
    private String url;
}
