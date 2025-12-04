package com.aaronjosh.real_estate_app.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.models.PropertyImageEntity;
import com.aaronjosh.real_estate_app.repositories.PropertyImageRepository;

@Service
public class FileService {

    @Autowired
    private PropertyImageRepository propertyImageRepo;

    public PropertyImageEntity getImageById(UUID id) {
        return propertyImageRepo.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));
    }
}
