package com.aaronjosh.real_estate_app.services;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.models.IdempotencyEntity;
import com.aaronjosh.real_estate_app.models.IdempotencyEntity.IdempotencyStatus;
import com.aaronjosh.real_estate_app.repositories.IdempotencyRepo;

import jakarta.transaction.Transactional;

@Service
public class IdempotencyService {

    @Autowired
    private IdempotencyRepo repo;

    @Transactional
    public ResponseEntity<?> handle(String key, Supplier<String> operation) {

        // If key already exists, fetch existing
        Optional<IdempotencyEntity> existingOpt = repo.findByIdempotencyKey(key);

        if (existingOpt.isPresent()) {
            IdempotencyEntity existing = existingOpt.get();

            if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
                // Already processed, return stored response
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                        "success", true,
                        "message", existing.getResponse()));
            } else {
                // Still processing
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Request is already in progress"));
            }
        }

        // Creates a idempotent entity
        IdempotencyEntity idemp = new IdempotencyEntity();
        idemp.setIdempotencyKey(key);
        idemp.setStatus(IdempotencyStatus.PENDING);

        repo.save(idemp);

        // Execute the operation
        String response = operation.get();

        // Update idempotency status to COMPLETED and store response
        idemp.setStatus(IdempotencyStatus.COMPLETED);
        idemp.setResponse(response);
        repo.save(idemp);

        // Return response
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", response));
    }
}