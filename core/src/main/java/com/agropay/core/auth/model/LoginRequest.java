package com.agropay.core.auth.model;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "{auth.username.not-blank}")
        String username,

        @NotBlank(message = "{auth.password.not-blank}")
        String password,

        String platform, 
        
        String profile 
) {
    public LoginRequest(String username, String password) {
        this(username, password, "WEB", null); 
    }
    
    public LoginRequest(String username, String password, String platform) {
        this(username, password, platform, null);
    }
}

