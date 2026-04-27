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
import com.aaronjosh.real_estate_app.dto.user.UserDetails;
import com.aaronjosh.real_estate_app.exceptions.EmailAlreadyExistsException;
import com.aaronjosh.real_estate_app.exceptions.PasswordNotMatchException;
import com.aaronjosh.real_estate_app.services.AuthService;
import com.aaronjosh.real_estate_app.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

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
        LoginResDto res = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity
                .ok(Map.of(
                        "success", true,
                        "message", "Login Successful",
                        "token", res.getToken(),
                        "userDetails", res));
    }

    /*
     * Handle the registration request and create new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReqDto userDto) {
        authService.register(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully created an account.");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully logout.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAuth() {
        UserDetails user = userService.getUserDetails();

        return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "Token is valid",
                "role", user.getRole(),
                "name", user.getFirstname() + " " + user.getLastname(),
                "email", user.getEmail(),
                "id", user.getId()));
    }
}
