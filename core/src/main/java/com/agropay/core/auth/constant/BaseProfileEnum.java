package com.agropay.core.auth.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum para los nombres de perfiles base del sistema.
 * Estos nombres deben coincidir EXACTAMENTE con los valores en las migraciones de base de datos.
 * 
 * IMPORTANTE: Este es el ÚNICO punto de verdad para los nombres de perfiles base.
 * NO hardcodee estos valores en otros lugares del código.
 */
@Getter
@RequiredArgsConstructor
public enum BaseProfileEnum {
    
    /**
     * Perfil base que todos los usuarios tendrán por defecto.
     * Permite ver perfil personal y consultar boletas de pago.
     * Definido en la migración V116__Create_colaborador_profile_and_elements.sql
     */
    COLABORADOR("Colaborador", "Perfil base para todos los colaboradores. Permite ver perfil personal y consultar boletas de pago"),
    
    /**
     * Perfil Administrador con acceso completo al sistema.
     * Definido en la migración V103__Insert_initial_containers_elements_profiles.sql
     */
    ADMINISTRADOR("Administrador", "Perfil con acceso completo al sistema"),
    
    /**
     * Perfil Supervisor para supervisores de campo.
     * Definido en la migración V103__Insert_initial_containers_elements_profiles.sql
     */
    SUPERVISOR("Supervisor", "Perfil para supervisores de campo"),
    
    /**
     * Perfil RRHH para personal de Recursos Humanos.
     * Definido en la migración V103__Insert_initial_containers_elements_profiles.sql
     */
    RRHH("RRHH", "Perfil para personal de Recursos Humanos");
    
    /**
     * Nombre del perfil tal como está almacenado en la base de datos (tbl_profiles.name)
     */
    private final String name;
    
    /**
     * Descripción del perfil
     */
    private final String description;
    
    /**
     * Obtiene el enum por nombre del perfil
     * @param profileName Nombre del perfil
     * @return El enum correspondiente o null si no existe
     */
    public static BaseProfileEnum findByName(String profileName) {
        if (profileName == null) {
            return null;
        }
        for (BaseProfileEnum profile : values()) {
            if (profile.getName().equals(profileName)) {
                return profile;
            }
        }
        return null;
    }
    
    /**
     * Verifica si un nombre de perfil corresponde a un perfil base
     * @param profileName Nombre del perfil a verificar
     * @return true si es un perfil base, false en caso contrario
     */
    public static boolean isBaseProfile(String profileName) {
        return findByName(profileName) != null;
    }
}

