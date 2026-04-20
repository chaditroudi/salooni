package com.glowzi.identity.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {

    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$",
             message = "Phone must be in E.164 format (e.g. +966501234567)")
    private String phone;

    @NotBlank
    private String password;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}