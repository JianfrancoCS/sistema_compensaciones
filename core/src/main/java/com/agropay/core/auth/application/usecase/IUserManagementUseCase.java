package com.agropay.core.auth.application.usecase;

import com.agropay.core.auth.model.user.AssignElementsRequest;
import com.agropay.core.auth.model.user.CreateUserRequest;
import com.agropay.core.auth.model.user.ProfileForAssignmentDTO;
import com.agropay.core.auth.model.user.SyncUserProfilesRequest;
import com.agropay.core.auth.model.user.UpdateUserStatusRequest;
import com.agropay.core.auth.model.user.UserDetailsDTO;
import com.agropay.core.auth.model.user.UserElementsByContainerDTO;
import com.agropay.core.auth.model.user.UserListDTO;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IUserManagementUseCase {
    
    /**
     * Crea un nuevo usuario en el sistema.
     * El perfil base "Colaborador" se asignará automáticamente.
     * 
     * @param request Datos del usuario a crear
     * @return UUID público del usuario creado
     */
    UUID create(CreateUserRequest request);
    
    /**
     * Obtiene una lista paginada de usuarios con filtros opcionales
     */
    PagedResult<UserListDTO> findAllPaged(String search, Boolean isActive, UUID positionId, Pageable pageable);
    
    /**
     * Obtiene todos los elementos disponibles agrupados por contenedor,
     * indicando cuáles están asignados directamente al usuario
     */
    UserElementsByContainerDTO getElementsByContainer(UUID userPublicId);
    
    /**
     * Asigna elementos directamente a un usuario.
     * Reemplaza las asignaciones anteriores.
     * Esto invalidará el token del usuario para que deba volver a loguearse.
     */
    void assignElements(UUID userPublicId, AssignElementsRequest request);
    
    /**
     * Obtiene todos los perfiles disponibles con estado de selección para un usuario.
     * Retorna todos los perfiles del sistema indicando cuáles están asignados al usuario.
     */
    List<ProfileForAssignmentDTO> getProfilesForAssignment(UUID userPublicId);
    
    /**
     * Sincroniza los perfiles asignados a un usuario.
     * Reemplaza todas las asignaciones anteriores con los perfiles proporcionados.
     */
    void syncUserProfiles(UUID userPublicId, SyncUserProfilesRequest request);
    
    /**
     * Actualiza el estado (activo/inactivo) de un usuario.
     * Si el usuario está inactivo, no podrá iniciar sesión.
     */
    void updateUserStatus(UUID userPublicId, UpdateUserStatusRequest request);
    
    /**
     * Obtiene los detalles completos de un usuario incluyendo información del empleado y contrato.
     * Si el usuario no tiene empleado asociado, retorna null en employee y contract.
     */
    UserDetailsDTO getUserDetails(UUID userPublicId);
}

