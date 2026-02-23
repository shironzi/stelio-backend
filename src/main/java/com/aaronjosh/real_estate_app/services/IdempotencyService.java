package com.aaronjosh.real_estate_app.services;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.models.IdempotencyEntity;
import com.aaronjosh.real_estate_app.models.IdempotencyEntity.IdempotencyStatus;
import com.aaronjosh.real_estate_app.repositories.IdempotencyRepo;

@Service
public class IdempotencyService {

    @Autowired
    private IdempotencyRepo repo;

    public Map<String, Object> handle(String key, Supplier<String> operation) {

        Optional<IdempotencyEntity> existing = repo.findByKey(key);

        if (existing.isPresent()) {
            // Return stored response if key already exists
            return Map.of(
                    "success", true,
                    "message", existing.get().getResponse());
        }

        // Execute the operation
        String response = operation.get();

        // Store response
        IdempotencyEntity idemp = new IdempotencyEntity();
        idemp.setKey(key);
        idemp.setResponse(response);
        idemp.setStatus(IdempotencyStatus.COMPLETED);
        repo.save(idemp);

        // Return response
        return Map.of(
                "success", true,
                "message", response);
    }
}