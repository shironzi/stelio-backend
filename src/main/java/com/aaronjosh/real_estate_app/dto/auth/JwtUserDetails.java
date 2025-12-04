package com.aaronjosh.real_estate_app.dto.auth;

import java.util.UUID;

public record JwtUserDetails(UUID userId, String email, String role) {
}
