package com.glowzi.identity.interfaces.rest.dto;

public class AuthResponse {
    private final Long userId;
    private final String role;
    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;

    public AuthResponse(Long userId, String role,
                        String accessToken, String refreshToken, long expiresIn) {
        this.userId = userId;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public Long getUserId()       { return userId; }
    public String getRole()       { return role; }
    public String getAccessToken()  { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public long getExpiresIn()    { return expiresIn; }
}

