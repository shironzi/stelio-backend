/*
 * Rest controller responsible for handling authentication endpoints (Login & Register).
 */

package com.aaronjosh.real_estate_app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aaronjosh.real_estate_app.dto.auth.LoginReqDto;
import com.aaronjosh.real_estate_app.dto.auth.LoginResDto;
import com.aaronjosh.real_estate_app.dto.auth.RegisterReqDto;
import com.aaronjosh.real_estate_app.exceptions.EmailAlreadyExistsException;
import com.aaronjosh.real_estate_app.exceptions.PasswordNotMatchException;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.services.AuthService;
import com.aaronjosh.real_estate_app.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    /*
     * Handles login request and returns JWT if credentials are valid.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReqDto request) {
        try {
            LoginResDto res = authService.login(request.getEmail(), request.getPassword());
            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Login Successful", "token", res.getToken(), "name",
                            res.getName(), "email", res.getEmail(), "role", res.getRole()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Server error"));
        }
    }

    /*
     * Handle the registration request and create new user account.
     */

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReqDto userDto) {
        try {
            authService.register(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully created an account.");
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (PasswordNotMatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            authService.logout(request);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully logout.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAuth() {
        try {
            UserEntity user = userService.getUserEntity();

            return ResponseEntity.ok().body(Map.of("success", true, "message", "Token is valid", "role", user.getRole(),
                    "name", user.getFirstname() + user.getLastname(), "email", user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
