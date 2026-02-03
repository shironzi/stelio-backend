package com.aaronjosh.real_estate_app.services;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.auth.UserDetails;
import com.aaronjosh.real_estate_app.models.FavoriteEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.FavoriteRepository;
import com.aaronjosh.real_estate_app.repositories.PropertyRepository;
import com.aaronjosh.real_estate_app.repositories.UserRepository;

@Service

public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepo;

    @Autowired
    private PropertyRepository propertyRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    // Checks if a favorite with the given UUID exists
    public boolean getFavorite(UUID id) {
        return favoriteRepo.findById(Objects.requireNonNull(id)).isPresent();
    }

    // Adds a favorite for the current user and the specified property.
    public void addFavorite(UUID propertyId) {
        UserDetails user = userService.getUserDetails();

        PropertyEntity property = propertyRepo.findById(Objects.requireNonNull(propertyId))
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // checks if the favorite is alreadt exists.
        Boolean existingFavorite = favoriteRepo.findByProperty_IdAndUser_Id(property.getId(),
                user.getId()).isPresent();

        UserEntity userEntity = userRepo.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (!existingFavorite) {
            FavoriteEntity favorite = new FavoriteEntity();
            userEntity.addFavorite(favorite);
            property.addFavorite(favorite);
            favoriteRepo.save(favorite);
        }
    }

    // Removes a favorite entry by its UUID
    public void removeFavorite(UUID propertyId) {
        UserDetails user = userService.getUserDetails();

        favoriteRepo.findByProperty_IdAndUser_Id(propertyId, user.getId())
                .ifPresent(favorite -> favoriteRepo.delete(Objects.requireNonNull(favorite)));
    }
}
