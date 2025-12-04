package com.aaronjosh.real_estate_app.dto.auth;

import com.aaronjosh.real_estate_app.models.UserEntity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResDto {
    private String token;
    private String name;
    private String email;
    private Role role;

    public LoginResDto() {

    }
}
