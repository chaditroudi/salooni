package com.glowzi.identity.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /auth/refresh.
 * The client sends the refresh token they received at login.
 */
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
