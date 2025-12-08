package com.aaronjosh.real_estate_app.services;

import java.util.Objects;
import java.util.UUID;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    public UserEntity getUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();

        UserEntity user = userRepo.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new RuntimeErrorException(null));

        return user;
    }
}