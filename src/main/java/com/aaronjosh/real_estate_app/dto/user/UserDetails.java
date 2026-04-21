package com.aaronjosh.real_estate_app.dto.user;

import java.util.UUID;

import com.aaronjosh.real_estate_app.models.UserEntity.Role;
import lombok.Data;

@Data
public class UserDetails {
    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
}
