package com.aaronjosh.real_estate_app.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.services.FavoriteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/{favoriteId}")
    public ResponseEntity<?> getFavorite(@Valid @PathVariable UUID favoriteId) {
        Boolean favorite = favoriteService.getFavorite(favoriteId);

        return ResponseEntity
                .ok(Map.of("success", true, "message", "successfully retrieve favorite", "favorite", favorite));
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<?> addFavorite(@Valid @PathVariable UUID propertyId) {
        favoriteService.addFavorite(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Successfully added to favorites"));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<?> removeFavorite(@Valid @PathVariable UUID propertyId) {
        favoriteService.removeFavorite(propertyId);

        return ResponseEntity.ok(Map.of("success", true, "message", "successfully removed from favorites"));
    }

}
