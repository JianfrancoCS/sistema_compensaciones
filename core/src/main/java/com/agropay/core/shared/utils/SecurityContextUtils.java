package com.agropay.core.shared.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilidad para obtener información del usuario autenticado desde el SecurityContext.
 */
public class SecurityContextUtils {

    private static final String SYSTEM_USER = "SYSTEM";

    /**
     * Obtiene el username del usuario autenticado desde el SecurityContext.
     * Si no hay usuario autenticado, retorna "SYSTEM".
     * 
     * El username debe ser compatible con los campos de auditoría en la BD:
     * - created_by, updated_by, deleted_by son NVARCHAR(100)
     * - El username en tbl_users también es NVARCHAR(100)
     * 
     * Si el username excede 100 caracteres, se trunca para evitar errores de BD.
     *
     * @return El username del usuario autenticado (máximo 100 caracteres) o "SYSTEM" si no hay autenticación
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return SYSTEM_USER;
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            return SYSTEM_USER;
        }
        // Asegurar que el username no exceda 100 caracteres (límite de NVARCHAR(100) en BD)
        // Esto es una medida de seguridad, aunque el username en UserEntity ya está limitado a 100
        if (username.length() > 100) {
            return username.substring(0, 100);
        }
        return username;
    }

    /**
     * Verifica si hay un usuario autenticado en el SecurityContext.
     *
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
}

