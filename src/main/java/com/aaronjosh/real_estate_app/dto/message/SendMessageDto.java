package com.aaronjosh.real_estate_app.dto.message;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SendMessageDto {
    private String message;
    private List<MultipartFile> files;
}
