package com.agropay.core.auth.web;

import com.agropay.core.auth.application.usecase.IUserUseCase;
import com.agropay.core.auth.model.ChangePasswordRequest;
import com.agropay.core.auth.model.LoginRequest;
import com.agropay.core.auth.model.LoginResponse;
import com.agropay.core.auth.model.RefreshTokenRequest;
import com.agropay.core.organization.application.usecase.IEmployeeUseCase;
import com.agropay.core.organization.model.employee.EmployeeMeResponse;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación y perfil de usuario")
public class AuthController {

    private final IUserUseCase userUseCase;
    private final IEmployeeUseCase employeeUseCase;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT")
    public ResponseEntity<ApiResult<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request - username: {}, platform: '{}', profile: '{}'", 
                request.username(), request.platform(), request.profile());
        LoginResponse response = userUseCase.login(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Renueva el access token usando un refresh token válido")
    public ResponseEntity<ApiResult<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = userUseCase.refreshToken(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario actual")
    public ResponseEntity<ApiResult<Void>> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        userUseCase.logout(username);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener información del usuario logueado", description = "Retorna la información del empleado asociado al usuario autenticado. Si el usuario no tiene empleado asociado (ej: admin), retorna valores por defecto.")
    public ResponseEntity<ApiResult<EmployeeMeResponse>> getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // username = document_number

        try {
            EmployeeMeResponse response = employeeUseCase.getMyInfo(username);
            return ResponseEntity.ok(ApiResult.success(response));
        } catch (com.agropay.core.shared.exceptions.IdentifierNotFoundException e) {
            // Si el usuario no tiene empleado asociado (ej: admin), retornar valores por defecto
            EmployeeMeResponse defaultResponse = new EmployeeMeResponse(
                    null, // code
                    username, // documentNumber
                    null, // names
                    null, // paternalLastname
                    null, // maternalLastname
                    null, // dob
                    null, // gender
                    null, // positionName
                    null, // subsidiaryId
                    null, // subsidiaryName
                    null  // photoUrl
            );
            return ResponseEntity.ok(ApiResult.success(defaultResponse));
        }
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar contraseña", description = "Permite al usuario autenticado cambiar su contraseña")
    public ResponseEntity<ApiResult<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        userUseCase.changePassword(username, request);
        return ResponseEntity.ok(ApiResult.success(null));
    }
}