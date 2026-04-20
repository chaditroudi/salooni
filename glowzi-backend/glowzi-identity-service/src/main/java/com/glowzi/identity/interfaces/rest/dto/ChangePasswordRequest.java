package com.glowzi.identity.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /auth/change-password.
 * The authenticated user provides their old password and new password.
 */
public class ChangePasswordRequest {

    @NotBlank(message = "Old password must not be blank")
    private String oldPassword;

    @NotBlank(message = "New password must not be blank")
    @Size(min = 8, max = 64, message = "New password must be between 8 and 64 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "New password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String newPassword;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
