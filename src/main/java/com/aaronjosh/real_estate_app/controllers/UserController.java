package com.aaronjosh.real_estate_app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aaronjosh.real_estate_app.services.UserService;

@Controller
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PatchMapping("/")
    @PreAuthorize("RENTER")
    public ResponseEntity<?> becomeHost() {
        userService.becomeHost();

        return ResponseEntity.ok(Map.of("success", true, "message", "Successfully become a host."));
    }
}
