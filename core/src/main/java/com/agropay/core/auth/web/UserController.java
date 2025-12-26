package com.agropay.core.auth.web;

import com.agropay.core.auth.application.usecase.IUserManagementUseCase;
import com.agropay.core.auth.model.user.AssignElementsRequest;
import com.agropay.core.auth.model.user.CreateUserRequest;
import com.agropay.core.auth.model.user.ProfileForAssignmentDTO;
import com.agropay.core.auth.model.user.SyncUserProfilesRequest;
import com.agropay.core.auth.model.user.UpdateUserStatusRequest;
import com.agropay.core.auth.model.user.UserDetailsDTO;
import com.agropay.core.auth.model.user.UserElementsByContainerDTO;
import com.agropay.core.auth.model.user.UserListDTO;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Gestión de Usuarios", description = "Operaciones para administrar usuarios del sistema.")
@Slf4j
public class UserController {

    private final IUserManagementUseCase userManagementService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear un nuevo usuario", 
               description = "Crea un nuevo usuario en el sistema. El perfil base 'Colaborador' se asignará automáticamente. Se puede asociar opcionalmente a un empleado existente mediante employeeId y validar el cargo mediante positionId.")
    public ResponseEntity<ApiResult<UUID>> create(@RequestBody @Valid CreateUserRequest request) {
        log.info("REST request to create User with username: {}", request.username());
        UUID userPublicId = userManagementService.create(request);
        return ResponseEntity.ok(ApiResult.success(userPublicId));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener listado paginado de usuarios", 
               description = "Devuelve una lista paginada de todos los usuarios. Permite la búsqueda por número de documento y filtro por cargo y estado activo/inactivo.")
    public ResponseEntity<ApiResult<PagedResult<UserListDTO>>> getPagedList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID positionId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {
        log.info("REST request to get all Users with search: '{}', isActive: {}, positionId: {}, sortBy: {}, sortDirection: {}", 
                search, isActive, positionId, sortBy, sortDirection);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResult<UserListDTO> response = userManagementService.findAllPaged(search, isActive, positionId, pageable);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/{publicId}/elements-by-container")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener elementos agrupados por contenedor para un usuario", 
               description = "Obtiene todos los elementos disponibles agrupados por contenedor, indicando cuáles están asignados al usuario a través de sus perfiles activos.")
    public ResponseEntity<ApiResult<UserElementsByContainerDTO>> getElementsByContainer(
            @PathVariable("publicId") UUID publicId) {
        UserElementsByContainerDTO response = userManagementService.getElementsByContainer(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PostMapping("/{publicId}/elements")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Asignar elementos a un usuario", 
               description = "Asigna elementos a un usuario creando o actualizando un perfil personalizado. El usuario deberá volver a loguearse para ver los cambios.")
    public ResponseEntity<ApiResult<Void>> assignElements(
            @PathVariable("publicId") UUID publicId,
            @RequestBody @Valid AssignElementsRequest request) {
        userManagementService.assignElements(publicId, request);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}/profiles-for-assignment")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener todos los perfiles disponibles con estado de selección para un usuario", 
               description = "Obtiene todos los perfiles activos del sistema indicando cuáles están asignados al usuario.")
    public ResponseEntity<ApiResult<List<ProfileForAssignmentDTO>>> getProfilesForAssignment(
            @PathVariable("publicId") UUID publicId) {
        List<ProfileForAssignmentDTO> response = userManagementService.getProfilesForAssignment(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PutMapping("/{publicId}/profiles")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Sincronizar perfiles asignados a un usuario", 
               description = "Sincroniza los perfiles asignados a un usuario. Reemplaza todas las asignaciones anteriores con los perfiles proporcionados.")
    public ResponseEntity<ApiResult<Void>> syncUserProfiles(
            @PathVariable("publicId") UUID publicId,
            @RequestBody @Valid SyncUserProfilesRequest request) {
        userManagementService.syncUserProfiles(publicId, request);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @PatchMapping("/{publicId}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar estado de un usuario", 
               description = "Actualiza el estado activo/inactivo de un usuario. Si el usuario está inactivo, no podrá iniciar sesión.")
    public ResponseEntity<ApiResult<Void>> updateUserStatus(
            @PathVariable("publicId") UUID publicId,
            @RequestBody @Valid UpdateUserStatusRequest request) {
        userManagementService.updateUserStatus(publicId, request);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}/details")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener detalles completos de un usuario", 
               description = "Obtiene información completa del usuario incluyendo datos del empleado asociado y contrato activo. Si el usuario no tiene empleado, retorna null en employee y contract.")
    public ResponseEntity<ApiResult<UserDetailsDTO>> getUserDetails(
            @PathVariable("publicId") UUID publicId) {
        UserDetailsDTO response = userManagementService.getUserDetails(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}

