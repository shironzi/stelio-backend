package com.aaronjosh.real_estate_app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.auth.LoginResDto;
import com.aaronjosh.real_estate_app.dto.user.UserDetails;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.UserEntity.Role;
import com.aaronjosh.real_estate_app.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

    public UserEntity getUser() {
        UserDetails userDetails = getUserDetails();

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    // Updating role into owner
    public LoginResDto becomeHost() {
        UserEntity user = getUser();

        user.setRole(Role.OWNER);
        userRepository.save(user);

        LoginResDto loginData = new LoginResDto();

        loginData.setName(user.getFullName());
        loginData.setEmail(user.getEmail());
        loginData.setRole(user.getRole().toString());
        loginData.setId(user.getId());

        return loginData;
    }
}