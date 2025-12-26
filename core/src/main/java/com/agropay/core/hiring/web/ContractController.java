package com.agropay.core.hiring.web;

import com.agropay.core.hiring.application.usecase.IContractUseCase;
import com.agropay.core.hiring.model.contract.*;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(ContractController.BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Contratos", description = "API para la gestión de contratos")
public class ContractController {

    public static final String BASE_URL = "/v1/hiring/contracts";

    private final IContractUseCase contractUseCase;

    @Operation(summary = "Crear un nuevo contrato")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResult<CommandContractResponse>> createContract(
            @RequestPart("contract") @Valid CreateContractRequest request,
            @RequestPart(value = "photo", required = true) MultipartFile photo) {
        log.info("Solicitud REST para crear Contrato: {}", request);
        CommandContractResponse response = contractUseCase.create(request, photo);
        return new ResponseEntity<>(ApiResult.success(response, "Contrato creado exitosamente"), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un contrato existente")
    @PutMapping("/{publicId}")
    public ResponseEntity<ApiResult<CommandContractResponse>> updateContract(@PathVariable UUID publicId, @Valid @RequestBody UpdateContractRequest request) {
        log.info("Solicitud REST para actualizar Contrato {} : {}", publicId, request);
        CommandContractResponse response = contractUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response, "Contrato actualizado exitosamente"));
    }

    @Operation(summary = "Obtener detalles del contrato por ID público")
    @GetMapping("/{publicId}/details")
    public ResponseEntity<ApiResult<ContractDetailsDTO>> getContractDetails(@PathVariable UUID publicId) {
        log.info("Solicitud REST para obtener detalles del Contrato: {}", publicId);
        ContractDetailsDTO response = contractUseCase.getDetailsByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener contrato para respuesta de comando por ID público")
    @GetMapping("/{publicId}/command")
    public ResponseEntity<ApiResult<CommandContractResponse>> getContractCommandResponse(@PathVariable UUID publicId) {
        log.info("Solicitud REST para obtener respuesta de comando de Contrato: {}", publicId);
        CommandContractResponse response = contractUseCase.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener todos los contratos con paginación y filtros")
    @GetMapping
    public ResponseEntity<ApiResult<PagedResult<ContractListDTO>>> getAllContracts(
            @Valid @ModelAttribute ContractPageableRequest request
    ) {
        log.info("Solicitud REST para obtener todos los Contratos con filtros: {}", request);
        PagedResult<ContractListDTO> result = contractUseCase.findAllPaged(
                request.getContractNumber(),
                request.getDocumentNumber(),
                request.getContractTypePublicId(),
                request.getStatePublicId(),
                request.toPageable()
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/states/select-options")
    @Operation(summary = "Obtener estados para campos de selección de contratos", description = "Devuelve una lista simplificada de estados para ser utilizada en componentes de UI.")
    public ResponseEntity<ApiResult<List<StateSelectOptionDTO>>> getStatesForSelect() {
        List<StateSelectOptionDTO> response = contractUseCase.getStatesSelectOptions();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    /**
     * @deprecated Este endpoint está deprecado. Use POST /v1/internal-files/upload directamente.
     */
    @Deprecated
    @Operation(summary = "[DEPRECADO] Generar URL de subida para un archivo de contrato")
    @PostMapping("/{publicId}/upload-url")
    public ResponseEntity<ApiResult<UploadUrlResponse>> generateUploadUrl(@PathVariable UUID publicId, @Valid @RequestBody GenerateUploadUrlRequest request) {
        log.warn("Endpoint deprecado: generateUploadUrl. Use POST /v1/internal-files/upload directamente.");
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ApiResult.failure("Este endpoint está deprecado. Use POST /v1/internal-files/upload directamente."));
    }

    /**
     * @deprecated Este endpoint está deprecado. Use POST /{publicId}/upload-file directamente.
     */
    @Deprecated
    @Operation(summary = "[DEPRECADO] Adjuntar archivo a un contrato")
    @PostMapping("/{publicId}/attach-file")
    public ResponseEntity<ApiResult<Void>> attachFile(@PathVariable UUID publicId, @Valid @RequestBody AttachFileRequest request) {
        log.warn("Endpoint deprecado: attachFile. Use POST /{publicId}/upload-file directamente.");
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ApiResult.failure("Este endpoint está deprecado. Use POST /{publicId}/upload-file directamente."));
    }

    @Operation(summary = "Subir y adjuntar archivo a un contrato")
    @PostMapping("/{publicId}/upload-file")
    public ResponseEntity<ApiResult<Void>> uploadFile(
            @PathVariable UUID publicId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        log.info("Solicitud REST para subir archivo al contrato {}", publicId);
        contractUseCase.uploadFile(publicId, file, description);
        return ResponseEntity.ok(ApiResult.success(null, "Archivo subido y adjuntado exitosamente"));
    }

    @Operation(summary = "Buscar contrato por número de documento de persona")
    @GetMapping("/search-by-person-document")
    public ResponseEntity<ApiResult<ContractSearchDTO>> searchByPersonDocumentNumber(@RequestParam String personDocumentNumber) {
        log.info("Solicitud REST para buscar contrato por número de documento: {}", personDocumentNumber);
        Optional<ContractSearchDTO> contract = contractUseCase.searchByPersonDocumentNumber(personDocumentNumber);

        if (contract.isEmpty()) {
            return ResponseEntity.ok(ApiResult.success(null, "Contrato no encontrado"));
        }

        return ResponseEntity.ok(ApiResult.success(contract.get(), "Contrato encontrado exitosamente"));
    }

    @Operation(summary = "Cancelar un contrato")
    @PatchMapping("/{publicId}/cancel")
    public ResponseEntity<ApiResult<Void>> cancelContract(@PathVariable UUID publicId) {
        log.info("Solicitud REST para cancelar contrato {}", publicId);
        contractUseCase.cancelContract(publicId);
        return ResponseEntity.ok(ApiResult.success(null, "Contrato cancelado exitosamente"));
    }

    @Operation(summary = "Obtener contenido renderizado del contrato con todas las variables")
    @GetMapping("/{publicId}/content")
    public ResponseEntity<ApiResult<ContractContentDTO>> getContractContent(@PathVariable UUID publicId) {
        log.info("Solicitud REST para obtener contenido renderizado del contrato: {}", publicId);
        ContractContentDTO response = contractUseCase.getContractContent(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Firmar un contrato con la firma del empleado",
               description = "Acepta una imagen de la firma del empleado (capturada o subida) y la asocia al contrato")
    @PatchMapping(value = "/{publicId}/sign", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResult<Void>> signContract(
            @PathVariable UUID publicId,
            @RequestParam("signature") MultipartFile signatureFile) {
        log.info("Solicitud REST para firmar contrato {} con firma del empleado", publicId);
        contractUseCase.signContract(publicId, signatureFile);
        return ResponseEntity.ok(ApiResult.success(null, "Contrato firmado exitosamente"));
    }
}
