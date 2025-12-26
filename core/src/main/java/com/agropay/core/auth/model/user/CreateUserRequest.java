package com.agropay.core.auth.model.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Request DTO para crear un nuevo usuario.
 * El perfil base "Colaborador" se asignará automáticamente.
 */
public record CreateUserRequest(
        @NotBlank(message = "El nombre de usuario es requerido")
        @Size(min = 3, message = "El nombre de usuario debe tener al menos 3 caracteres")
        String username,
        
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,
        
        /**
         * UUID del cargo (position) - opcional
         */
        UUID positionId,
        
        /**
         * Número de documento del empleado asociado - opcional
         * NULL para usuarios admin que no son empleados
         */
        String employeeId
) {
}

