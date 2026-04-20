package com.glowzi.identity.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for POST /auth/logout.
 * Client sends the refresh token to invalidate.
 */
public class LogoutRequest {

    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
