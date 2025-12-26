package com.agropay.core.auth.application.usecase;

import com.agropay.core.auth.model.ChangePasswordRequest;
import com.agropay.core.auth.model.LoginRequest;
import com.agropay.core.auth.model.LoginResponse;
import com.agropay.core.auth.model.RefreshTokenRequest;

public interface IUserUseCase {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void logout(String username);
    void changePassword(String username, ChangePasswordRequest request);
}

