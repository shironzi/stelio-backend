package com.aaronjosh.real_estate_app.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.models.FavoriteEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.FavoriteRepository;
import com.aaronjosh.real_estate_app.repositories.PropertyRepository;

@Service

public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepo;

    @Autowired
    private PropertyRepository propertyRepo;

    @Autowired
    private UserService userService;

    // Checks if a favorite with the given UUID exists
    public boolean getFavorite(UUID id) {
        return favoriteRepo.findById(id).isPresent();
    }

    // Adds a favorite for the current user and the specified property.
    public void addFavorite(UUID propertyId) {
        UserEntity user = userService.getUserEntity();

        PropertyEntity property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // checks if the favorite is alreadt exists.
        Optional<FavoriteEntity> existingFavorite = favoriteRepo.findByProperty_IdAndUser_Id(property.getId(),
                user.getId());

        if (existingFavorite.isEmpty()) {
            FavoriteEntity favorite = new FavoriteEntity();
            user.addFavorite(favorite);
            property.addFavorite(favorite);
            favoriteRepo.save(favorite);
        }
    }

    // Removes a favorite entry by its UUID
    public void removeFavorite(UUID propertyId) {
        UserEntity user = userService.getUserEntity();
        if (user == null)
            return;

        favoriteRepo.findByProperty_IdAndUser_Id(propertyId, user.getId())
                .ifPresent(favorite -> favoriteRepo.delete(favorite));
    }
}
