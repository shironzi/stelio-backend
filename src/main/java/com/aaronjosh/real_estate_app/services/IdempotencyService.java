package com.aaronjosh.real_estate_app.services;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.models.IdempotencyEntity;
import com.aaronjosh.real_estate_app.models.IdempotencyEntity.IdempotencyStatus;
import com.aaronjosh.real_estate_app.repositories.IdempotencyRepo;

@Service
public class IdempotencyService {

    @Autowired
    private IdempotencyRepo repo;

    public ResponseEntity<?> handle(String key, Supplier<String> operation) {
        try {
            // Creates a idempotent entity
            IdempotencyEntity idemp = new IdempotencyEntity();
            idemp.setIdempotencyKey(key);
            idemp.setStatus(IdempotencyStatus.PENDING);
            repo.saveAndFlush(idemp);

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

        } catch (DataIntegrityViolationException e) {
            // Another request inserted it first → fetch existing
            IdempotencyEntity existing = repo.findByIdempotencyKey(key)
                    .orElseThrow(() -> new IllegalStateException("Missing idempotency record"));

            if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                        "success", true,
                        "message", existing.getResponse()));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                        "success", true,
                        "message", "Request is already in progress"));
            }
        }
    }
}