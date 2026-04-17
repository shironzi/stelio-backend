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

    public ResponseEntity<?> handle(String key, Supplier<Map<String, Object>> operationResponse) {
        try {
            // Creates a idempotent entity
            IdempotencyEntity idemp = new IdempotencyEntity();
            idemp.setIdempotencyKey(key);
            idemp.setStatus(IdempotencyStatus.PENDING);
            idemp.setResponseMap(Map.of("success", true, "message", "Request is already in progress"));
            repo.saveAndFlush(idemp);

            // Execute the operation
            Map<String, Object> response = operationResponse.get();

            // Update idempotency status to COMPLETED and store response
            idemp.setStatus(IdempotencyStatus.COMPLETED);
            idemp.setResponseMap(response);
            repo.saveAndFlush(idemp);

            // Return response
            return ResponseEntity.status(HttpStatus.CREATED).body(operationResponse);

        } catch (DataIntegrityViolationException e) {
            // Another request inserted it first → fetch existing
            IdempotencyEntity existing = repo.findByIdempotencyKey(key)
                    .orElseThrow(() -> new IllegalStateException("Missing idempotency record"));

            return ResponseEntity.status(HttpStatus.OK).body(existing.getResponseMap());
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during idempotent operation", e);
        }
    }
}