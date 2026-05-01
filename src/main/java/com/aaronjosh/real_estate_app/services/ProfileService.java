package com.aaronjosh.real_estate_app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.user.UserProfileDetailsDto;
import com.aaronjosh.real_estate_app.models.FileEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ProfileService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    public UserProfileDetailsDto getProfile() {
        UserEntity user = userService.getUser();

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
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large");
        }

        UserEntity user = userService.getUser();

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
