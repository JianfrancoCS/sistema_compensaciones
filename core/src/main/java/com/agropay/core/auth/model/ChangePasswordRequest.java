package com.agropay.core.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "{auth.current-password.not-blank}")
        String currentPassword,
        
        @NotBlank(message = "{auth.new-password.not-blank}")
        @Size(min = 6, message = "{auth.new-password.size}")
        String newPassword
) {}

