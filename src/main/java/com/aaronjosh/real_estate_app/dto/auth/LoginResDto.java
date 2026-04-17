package com.aaronjosh.real_estate_app.dto.auth;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResDto {
    private String token;
    private String name;
    private String email;
    private String role;
    private UUID id;

    public LoginResDto() {

    }
}
