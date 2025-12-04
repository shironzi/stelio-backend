package com.aaronjosh.real_estate_app.dto.property;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class NewImagesDto {
    private MultipartFile image;
    private Integer position;
}
