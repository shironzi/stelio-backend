package com.aaronjosh.real_estate_app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.auth.LoginResDto;
import com.aaronjosh.real_estate_app.dto.user.UserDetails;
import com.aaronjosh.real_estate_app.dto.user.UserProfileDetailsDto;
import com.aaronjosh.real_estate_app.models.FileEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.UserEntity.Role;
import com.aaronjosh.real_estate_app.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    // fetching UserDetails from security layer
    public UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            }
        }
        return null;
    }

    // Updating role into owner
    public LoginResDto becomeHost() {
        UserDetails userDetails = getUserDetails();

        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        UserEntity user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setRole(Role.OWNER);
        userRepository.save(user);

        LoginResDto loginData = new LoginResDto();

        loginData.setName(user.getFullName());
        loginData.setEmail(user.getEmail());
        loginData.setRole(user.getRole().toString());
        loginData.setId(user.getId());

        return loginData;
    }

    public UserProfileDetailsDto getProfile() {
        UserDetails userDetails = getUserDetails();

        UserEntity user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfileDetailsDto dto = new UserProfileDetailsDto();

        // User Info
        dto.setFirstname(user.getFirstname());
        dto.setLastName(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCity(user.getCity());
        dto.setJoinedAt(user.getCreatedAt().toLocalDate());

        // Profile Picture
        if (user.getProfilePicture() != null) {
            dto.setProfileLink(cloudflareR2Service.generateLink(user.getProfilePicture()));
        }

        // Activity
        dto.setBookingsMade(0);
        dto.setNightStayed(0);
        dto.setUpcomingStays(0);
        dto.setReviewsGiven(0);
        dto.setTotalSpent(0);

        return dto;
    }

    @Transactional
    public void uploadProfilePic(MultipartFile file) {
        UserDetails userDetails = getUserDetails();

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large");
        }

        UserEntity user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        FileEntity fileEntity = new FileEntity();

        try {
            String path = "users/" + user.getId() + "/profile/";
            String key = cloudflareR2Service.uploadFile(file, false, path);

            fileEntity.setFilename(file.getOriginalFilename());
            fileEntity.setSize(file.getSize());
            fileEntity.setContentType(contentType);
            fileEntity.setKey(key);
            fileEntity.setIsPublic(false);
            fileEntity.setUploadedBy(user);

            user.setProfilePicture(fileEntity);
            userRepository.save(user);
        } catch (java.io.IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload profile picture", e);
        }
    }
}