package com.aaronjosh.real_estate_app.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.services.FileService;

@Controller
@RequestMapping("/api/image/")
public class FileController {
    @Autowired
    private FileService fileService;

    @GetMapping("{id}")
    public ResponseEntity<?> getImage(@PathVariable UUID id) {

        var image = fileService.getImageById(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf(image.getType())).body(image.getData());
    }
}
