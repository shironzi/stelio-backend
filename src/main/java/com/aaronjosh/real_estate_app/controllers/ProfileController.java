package com.aaronjosh.real_estate_app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.aaronjosh.real_estate_app.dto.user.UserProfileDetailsDto;
import com.aaronjosh.real_estate_app.services.ProfileService;

@Controller
@RequestMapping()
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {

        // Get the UserProfileDetailsDto from the service
        UserProfileDetailsDto res = profileService.getProfile();

        // Return the response with user details and activities
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully became a host.",
                "userDetails", res.getUserInfo(),
                "activities", res.getActivities()));
    }

    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("picture") MultipartFile file) {
        profileService.uploadProfilePic(file);

        return ResponseEntity.ok(Map.of("success", true, "message", "Successfully uploaded profile picture."));
    }
}
