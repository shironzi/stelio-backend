package com.aaronjosh.real_estate_app.util;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.aaronjosh.real_estate_app.models.FileEntity;

@Component
public class LinkGenerator {

    public String generateLink(FileEntity file) {
        if (file == null) {
            throw new IllegalArgumentException("FileEntity cannot be null");
        }

        String fileId = Objects.requireNonNull(file.getId(), "File ID cannot be null").toString();

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/image/")
                .path(fileId)
                .encode()
                .toUriString();
    }
}
