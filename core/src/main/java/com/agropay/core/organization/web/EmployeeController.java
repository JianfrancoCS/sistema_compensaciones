package com.agropay.core.organization.web;

import com.agropay.core.images.application.usecase.IFileStorageUseCase;
import com.agropay.core.images.application.usecase.IImageUseCase;
import com.agropay.core.images.constant.Bucket;
import com.agropay.core.images.domain.ImageEntity;
import com.agropay.core.organization.api.EmployeeStateEnum;
import com.agropay.core.organization.application.usecase.IEmployeeUseCase;
import com.agropay.core.organization.application.usecase.IPersonUseCase;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.model.employee.*;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión de Empleados", description = "Endpoints para el ciclo de vida completo de un empleado, desde su creación hasta su eliminación.")
public class EmployeeController {

    private final IEmployeeUseCase employeeService;
    private final IPersonUseCase personService;
    private final IFileStorageUseCase fileStorageService;
    private final IImageUseCase imageUseCase;

    @GetMapping("/by-document/{documentNumber}/validate")
    @Operation(summary = "Validar empleado por documento", description = "Devuelve true si el empleado existe, está ACTIVO y, si se envía subsidiaryPublicId, pertenece a esa sucursal.")
    public ResponseEntity<ApiResult<Boolean>> validateByDocument(
            @PathVariable String documentNumber,
            @RequestParam(required = false) UUID subsidiaryPublicId) {

        Optional<EmployeeEntity> employeeOpt = employeeService.findByPersonDocumentNumber(documentNumber);
        boolean isValid = employeeOpt.map(emp -> {
            boolean active = emp.getState() != null && EmployeeStateEnum.ACTIVO.getCode().equals(emp.getState().getCode());
            boolean subsidiaryOk = subsidiaryPublicId == null || (emp.getSubsidiary() != null && subsidiaryPublicId.equals(emp.getSubsidiary().getPublicId()));
            return active && subsidiaryOk;
        }).orElse(false);

        return ResponseEntity.ok(ApiResult.success(isValid));
    }

    @GetMapping("/states/select-options")
    @Operation(summary = "Obtener estados para campos de selección", description = "Devuelve una lista simplificada de estados (código y nombre) para ser utilizada en componentes de UI, como la selección del estado de un empleado.")
    public ResponseEntity<ApiResult<List<StateSelectOptionDTO>>> getStatesForSelect() {
        List<StateSelectOptionDTO> response = employeeService.getStatesSelectOptions();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo empleado", description = "Crea un nuevo registro de empleado en el sistema. Si la persona no existe, se crea automáticamente. Valida las reglas de negocio sobre supervisores.")
    public ResponseEntity<ApiResult<CommandEmployeeResponse>> create(@RequestBody @Valid CreateEmployeeRequest request) {
        CommandEmployeeResponse response = employeeService.create(request);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PatchMapping("/{code}")
    @Operation(summary = "Actualizar un empleado existente", description = "Actualiza los datos de un empleado existente, como su sucursal, posición o supervisor. Se identifica por su código único.")
    public ResponseEntity<ApiResult<CommandEmployeeResponse>> update(@PathVariable UUID code, @RequestBody @Valid UpdateEmployeeRequest request) {
        CommandEmployeeResponse response = employeeService.update(code, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PostMapping("/{code}/activate")
    @Operation(summary = "Activar un empleado", description = "Activa un empleado que se encuentra en estado 'Creado'. Valida que se cumplan todos los requisitos, como la asignación de un supervisor si es necesario.")
    public ResponseEntity<ApiResult<Void>> activateEmployee(@PathVariable UUID code) {
        employeeService.activateEmployee(code);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Buscar un empleado por código", description = "Obtiene los detalles completos de un empleado específico a través de su código único.")
    public ResponseEntity<ApiResult<EmployeeDetailsDTO>> getByCode(@PathVariable UUID code) {
        EmployeeDetailsDTO response = employeeService.getByCode(code);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/command/{publicId}")
    @Operation(summary = "Obtener un empleado para edición", description = "Obtiene los datos de un empleado, incluyendo IDs públicos de entidades relacionadas, para ser utilizada en formularios de edición.")
    public ResponseEntity<ApiResult<CommandEmployeeResponse>> getCommandResponseByPublicId(@PathVariable UUID publicId) {
        CommandEmployeeResponse response = employeeService.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtener listado paginado de empleados", description = "Devuelve una lista paginada de todos los empleados. Permite filtrar por número de documento, sucursal, posición y nacionalidad.")
    public ResponseEntity<ApiResult<PagedResult<EmployeeListDTO>>> getAll(
            @Valid @ModelAttribute EmployeePageableRequest request) {

        PagedResult<EmployeeListDTO> response = employeeService.findAllPaged(
                request.getDocumentNumber(),
                request.getSubsidiaryPublicId(),
                request.getPositionPublicId(),
                request.getIsNational(),
                request.toPageable()
        );
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/select-options")
    @Operation(summary = "Obtener empleados para campos de selección", description = "Devuelve una lista de empleados que pueden ser supervisores para una posición dada.")
    public ResponseEntity<ApiResult<List<EmployeeSelectOptionDTO>>> getSelectOptions(
            @Parameter(description = "ID público de la posición para la que se buscan supervisores.", required = true)
            @RequestParam UUID positionPublicId) {
        List<EmployeeSelectOptionDTO> response = employeeService.getSelectOptions(positionPublicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar empleado por número de documento", description = "Retorna la información del empleado si existe, sin importar su estado. Caso contrario, retorna error.")
    public ResponseEntity<ApiResult<EmployeeSearchResponse>> searchByDocumentNumber(
            @Parameter(description = "Número de documento del empleado a buscar", required = true)
            @RequestParam String documentNumber) {
        EmployeeSearchResponse response = employeeService.searchByDocumentNumber(documentNumber);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/cache")
    @Operation(summary = "Obtener datos de empleado para caché offline-first", description = "Retorna datos básicos del empleado (nombres, apellidos, subsidiaria) para almacenar en la base de datos local del móvil. Solo retorna empleados activos sin soft-delete.")
    public ResponseEntity<ApiResult<EmployeeCacheResponse>> getEmployeeCache(
            @Parameter(description = "Número de documento del empleado a buscar", required = true)
            @RequestParam String documentNumber) {
        EmployeeCacheResponse response = employeeService.getEmployeeCacheByDocumentNumber(documentNumber);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/organizational-chart")
    @Operation(summary = "Obtener el organigrama completo o de una sucursal", description = "Devuelve la estructura jerárquica de los empleados. Si no se especifica subsidiaryId, devuelve toda la estructura desde el CEO. Si se especifica, devuelve solo la rama de esa sucursal.")
    public ResponseEntity<ApiResult<List<OrganizationalChartNodeDTO>>> getOrganizationalChart(
            @Parameter(description = "ID público de la sucursal para filtrar el organigrama. Si no se proporciona, devuelve toda la estructura organizacional.", required = false)
            @RequestParam(required = false) UUID subsidiaryId,
            @Parameter(description = "Número de niveles jerárquicos a mostrar. Si no se especifica, muestra todos los niveles.", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer levels) {
        List<OrganizationalChartNodeDTO> chart = employeeService.getOrganizationalChart(subsidiaryId, levels);
        return ResponseEntity.ok(ApiResult.success(chart));
    }

    @PatchMapping("/{publicId}/subsidiary")
    @Operation(summary = "Actualizar la sucursal de un empleado", description = "Realiza una actualización parcial para cambiar la sucursal de un empleado.")
    public ResponseEntity<ApiResult<Void>> updateSubsidiary(@PathVariable UUID publicId, @RequestBody @Valid UpdateEmployeeSubsidiaryRequest request) {
        employeeService.updateSubsidiary(publicId, request);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @PatchMapping("/{publicId}/position")
    @Operation(summary = "Actualizar la posición de un empleado", description = "Realiza una actualización parcial para cambiar la posición y, opcionalmente, el supervisor de un empleado.")
    public ResponseEntity<ApiResult<Void>> updatePosition(@PathVariable UUID publicId, @RequestBody @Valid UpdateEmployeePositionRequest request) {
        employeeService.updatePosition(publicId, request);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @PostMapping("/{documentNumber}/photo")
    @Operation(summary = "Subir foto de persona/empleado", description = "Sube una foto a Cloudinary y la asocia a la persona. Reemplaza la foto anterior si existe.")
    public ResponseEntity<ApiResult<String>> uploadPhoto(
            @Parameter(description = "Número de documento de la persona", required = true)
            @PathVariable String documentNumber,
            @RequestParam("file") MultipartFile file) {
        
        log.info("Subiendo foto para persona con documentNumber: {}", documentNumber);
        
        try {
            // Buscar persona
            PersonEntity person = personService.findPersonByDocumentNumber(documentNumber, null);
            if (person == null) {
                throw new IdentifierNotFoundException("exception.organization.person.not-found", documentNumber);
            }

            // Subir a Cloudinary
            Map<String, Object> uploadResult = fileStorageService.uploadFile(file, Bucket.PERSON_PHOTO);
            String imageUrl = (String) uploadResult.get("secure_url");
            
            if (imageUrl == null) {
                imageUrl = (String) uploadResult.get("url");
            }

            // Guardar URL en la tabla de imágenes
            imageUseCase.attachImage(person, imageUrl);
            
            log.info("Foto subida exitosamente para persona {}. URL: {}", documentNumber, imageUrl);
            return ResponseEntity.ok(ApiResult.success(imageUrl, "Foto subida exitosamente"));
            
        } catch (Exception e) {
            log.error("Error subiendo foto para persona {}", documentNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("Error subiendo foto: " + e.getMessage()));
        }
    }

    @GetMapping("/{documentNumber}/photo")
    @Operation(summary = "Obtener foto de persona/empleado", description = "Retorna la URL de la foto de la persona si existe.")
    public ResponseEntity<ApiResult<String>> getPhoto(
            @Parameter(description = "Número de documento de la persona", required = true)
            @PathVariable String documentNumber) {
        
        log.info("Obteniendo foto para persona con documentNumber: {}", documentNumber);
        
        try {
            // Buscar persona
            PersonEntity person = personService.findPersonByDocumentNumber(documentNumber, null);
            if (person == null) {
                throw new IdentifierNotFoundException("exception.organization.person.not-found", documentNumber);
            }

            // Obtener imágenes
            List<ImageEntity> images = imageUseCase.getImagesByImageable(person);
            
            if (images.isEmpty()) {
                return ResponseEntity.ok(ApiResult.success(null, "No hay foto disponible"));
            }

            // Retornar la primera imagen (la más reciente)
            String imageUrl = images.get(0).getUrl();
            log.info("Foto encontrada para persona {}. URL: {}", documentNumber, imageUrl);
            return ResponseEntity.ok(ApiResult.success(imageUrl));
            
        } catch (Exception e) {
            log.error("Error obteniendo foto para persona {}", documentNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("Error obteniendo foto: " + e.getMessage()));
        }
    }
}
