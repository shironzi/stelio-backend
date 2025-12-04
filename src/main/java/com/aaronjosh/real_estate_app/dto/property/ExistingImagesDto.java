package com.aaronjosh.real_estate_app.dto.property;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExistingImagesDto {
    @NotNull
    private UUID id;

    @NotNull
    private Integer position;
}
