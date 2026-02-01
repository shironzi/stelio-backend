package com.aaronjosh.real_estate_app.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {}