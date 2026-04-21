package com.aaronjosh.real_estate_app.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.models.FileEntity;
import com.aaronjosh.real_estate_app.repositories.FileRepository;
import com.aaronjosh.real_estate_app.services.CloudflareR2Service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Controller
@RequestMapping("/api/files/")
public class FileController {
    @Autowired
    private FileRepository fileRepo;

    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable UUID id) {

        FileEntity file = fileRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        ResponseInputStream<GetObjectResponse> stream = cloudflareR2Service.loadFile(file.getKey());

        InputStreamResource body = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\"")
                .body(body);
    }
}
