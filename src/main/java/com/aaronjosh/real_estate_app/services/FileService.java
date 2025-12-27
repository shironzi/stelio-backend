package com.aaronjosh.real_estate_app.services;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.models.FileEntity;
import com.aaronjosh.real_estate_app.models.MessageEntity;
import com.aaronjosh.real_estate_app.repositories.PropertyImageRepository;

@Service
public class FileService {

    @Autowired
    private PropertyImageRepository propertyImageRepo;

    public FileEntity getImageById(UUID id) {
        return propertyImageRepo.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Image not found"));
    }

    public FileEntity mapToFileEntity(MultipartFile file, MessageEntity message) {
        try {
            FileEntity f = new FileEntity(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes());
            f.setMessage(message);
            return f;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Failed to read uploaded file",
                    e);
        }
    }

}
