package com.agropay.core.auth.application.service;

import com.agropay.core.auth.application.usecase.IUserUseCase;
import com.agropay.core.auth.domain.UserEntity;
import com.agropay.core.auth.domain.ProfileEntity;
import com.agropay.core.auth.domain.UserProfileEntity;
import com.agropay.core.auth.model.ChangePasswordRequest;
import com.agropay.core.auth.model.LoginRequest;
import com.agropay.core.auth.model.LoginResponse;
import com.agropay.core.auth.model.NavigationItemDTO;
import com.agropay.core.auth.persistence.IUserRepository;
import com.agropay.core.auth.persistence.IProfileRepository;
import com.agropay.core.auth.persistence.IUserProfileRepository;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserUseCase {

    private final IUserRepository userRepository;
    private final IProfileRepository profileRepository;
    private final IUserProfileRepository userProfileRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final MenuService menuService;

    @Value("${spring.security.jwt.expiration}")
    private Long jwtExpiration;

    @Value("${spring.security.jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.username());
        log.info("Login request - platform: '{}', profile: '{}'", request.platform(), request.profile());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

            String token = tokenProvider.generateToken(userDetails);
            String refreshToken = tokenProvider.generateRefreshToken(userDetails);

            UserEntity user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new BusinessValidationException("auth.user-not-found", request.username()));

            if (!user.getIsActive()) {
                log.warn("Inactive user {} attempted to login", request.username());
                throw new BusinessValidationException("auth.user-inactive", "El usuario está inactivo y no puede iniciar sesión.");
            }

            String platform = request.platform() != null && !request.platform().isBlank() 
                    ? request.platform().toUpperCase() 
                    : "WEB";
            
            log.info("Processing login for platform: '{}' (original: '{}')", platform, request.platform());
            
            if ("MOBILE".equals(platform) && request.profile() != null && !request.profile().isBlank()) {
                log.info("Validating profile '{}' for mobile login", request.profile());
                validateUserProfile(user, request.profile());
            } else {
                log.debug("Skipping profile validation - platform: '{}', profile: '{}'", platform, request.profile());
            }

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            List<NavigationItemDTO> menu = menuService.getMenuByUserId(user.getId(), platform);

            log.info("User {} logged in successfully with {} menu items for platform {}", request.username(), menu.size(), platform);

            return new LoginResponse(
                    token,
                    refreshToken,
                    user.getPublicId(),
                    user.getUsername(),
                    jwtExpiration / 1000, // Convertir a segundos
                    refreshTokenExpiration / 1000, // Convertir a segundos
                    menu
            );

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", request.username());
            throw new BusinessValidationException("auth.invalid-credentials");
        } catch (Exception e) {
            log.error("Error during login for user {}: {}", request.username(), e.getMessage());
            throw new BusinessValidationException("auth.login-failed", e.getMessage());
        }
    }
    
    private void validateUserProfile(UserEntity user, String profileName) {
        log.info("Validating profile '{}' for user {}", profileName, user.getUsername());
        
        var profileOpt = profileRepository.findByName(profileName);
        
        if (profileOpt.isEmpty()) {
            log.debug("Profile '{}' not found with exact match, trying case-insensitive search", profileName);
            java.util.List<ProfileEntity> allProfiles = profileRepository.findAll();
            profileOpt = allProfiles.stream()
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(profileName))
                    .findFirst();
        }
        
        var profile = profileOpt.orElseThrow(() -> {
            log.error("Profile '{}' not found in database for user {}", profileName, user.getUsername());
            return new BusinessValidationException("auth.profile-not-found", profileName);
        });
        
        log.debug("Found profile: {} (ID: {})", profile.getName(), profile.getId());
        
        boolean hasProfile = false;
        if (user.getProfileId() != null && user.getProfileId().equals(profile.getId())) {
            hasProfile = true;
            log.debug("User {} has profile {} as main profile", user.getUsername(), profileName);
        }
        
        if (!hasProfile) {
            hasProfile = userProfileRepository.existsActiveByUserIdAndProfileId(
                    user.getId(), profile.getId());
            if (hasProfile) {
                log.debug("User {} has profile {} as additional profile", user.getUsername(), profileName);
            } else {
                log.debug("User {} does NOT have profile {} as additional profile", user.getUsername(), profileName);
            }
        }
        
        if (!hasProfile) {
            log.warn("User {} does not have profile {} (ID: {}). Main profile ID: {}, Additional profiles will be checked", 
                    user.getUsername(), profileName, profile.getId(), user.getProfileId());
            
            java.util.List<UserProfileEntity> userProfiles = userProfileRepository.findAllProfilesByUserId(user.getId());
            log.warn("User {} has {} additional profiles assigned:", user.getUsername(), userProfiles.size());
            userProfiles.forEach(up -> {
                log.warn("  - Profile: {} (ID: {}), Active: {}, Deleted: {}", 
                        up.getProfile().getName(), 
                        up.getProfile().getId(),
                        up.getIsActive(),
                        up.getDeletedAt());
            });
            
            throw new BusinessValidationException("auth.user-profile-mismatch", profileName);
        }
        
        log.info("User {} successfully validated with profile {}", user.getUsername(), profileName);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(com.agropay.core.auth.model.RefreshTokenRequest request) {
        log.info("Attempting to refresh token");

        try {
            if (!tokenProvider.isRefreshToken(request.refreshToken())) {
                log.warn("Invalid refresh token provided");
                throw new BusinessValidationException("auth.invalid-refresh-token");
            }

            if (tokenProvider.isTokenExpired(request.refreshToken())) {
                log.warn("Refresh token has expired");
                throw new BusinessValidationException("auth.refresh-token-expired");
            }

            String username = tokenProvider.getUsernameFromToken(request.refreshToken());

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            String newToken = tokenProvider.generateToken(userDetails);
            String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessValidationException("auth.user-not-found", username));

            List<NavigationItemDTO> menu = menuService.getMenuByUserId(user.getId());

            log.info("Token refreshed successfully for user: {}", username);

            return new LoginResponse(
                    newToken,
                    newRefreshToken,
                    user.getPublicId(),
                    user.getUsername(),
                    jwtExpiration / 1000,
                    refreshTokenExpiration / 1000,
                    menu
            );

        } catch (BusinessValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage());
            throw new BusinessValidationException("auth.refresh-token-failed", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void logout(String username) {
        log.info("User {} logged out", username);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        log.info("Attempting to change password for user: {}", username);
        
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessValidationException("auth.user-not-found", username));
        
        if (!user.getIsActive()) {
            log.warn("Inactive user {} attempted to change password", username);
                throw new BusinessValidationException("auth.user-inactive", "El usuario está inactivo.");
        }
        
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            log.warn("Invalid current password for user: {}", username);
                throw new BusinessValidationException("auth.invalid-current-password", "La contraseña actual es incorrecta.");
        }
        
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            log.warn("New password is the same as current password for user: {}", username);
                throw new BusinessValidationException("auth.same-password", "La nueva contraseña debe ser diferente a la actual.");
        }
        
        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", username);
    }
}

