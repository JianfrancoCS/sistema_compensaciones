package com.agropay.core.hiring.web;

import com.agropay.core.hiring.application.usecase.IContractAddendumUseCase;
import com.agropay.core.hiring.model.addendum.*;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AddendumController.BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Adendas", description = "API para la gestión de adendas de contrato")
public class AddendumController {

    public static final String BASE_URL = "/v1/hiring/addendums";

    private final IContractAddendumUseCase addendumUseCase;

    @Operation(summary = "Crear una nueva adenda")
    @PostMapping
    public ResponseEntity<ApiResult<CommandAddendumResponse>> create(@Valid @RequestBody CreateAddendumRequest request) {
        log.info("REST request to create Addendum: {}", request);
        CommandAddendumResponse response = addendumUseCase.create(request);
        return new ResponseEntity<>(ApiResult.success(response, "Adenda creada exitosamente."), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar una adenda existente")
    @PutMapping("/{publicId}")
    public ResponseEntity<ApiResult<CommandAddendumResponse>> update(@PathVariable UUID publicId, @Valid @RequestBody UpdateAddendumRequest request) {
        log.info("REST request to update Addendum {}: {}", publicId, request);
        CommandAddendumResponse response = addendumUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response, "Adenda actualizada exitosamente."));
    }

    @Operation(summary = "Eliminar una adenda por ID público")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable UUID publicId) {
        log.info("REST request to delete Addendum: {}", publicId);
        addendumUseCase.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null, "Adenda eliminada exitosamente."));
    }

    @Operation(summary = "Obtener detalles de la adenda por ID público")
    @GetMapping("/{publicId}/details")
    public ResponseEntity<ApiResult<AddendumDetailsDTO>> getDetails(@PathVariable UUID publicId) {
        log.info("REST request to get details for Addendum: {}", publicId);
        AddendumDetailsDTO response = addendumUseCase.getDetailsByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener adenda para formulario por ID público")
    @GetMapping("/{publicId}/command")
    public ResponseEntity<ApiResult<CommandAddendumResponse>> getCommandResponse(@PathVariable UUID publicId) {
        log.info("REST request to get command response for Addendum: {}", publicId);
        CommandAddendumResponse response = addendumUseCase.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener contenido de adenda por ID público")
    @GetMapping("/{publicId}/content")
    public ResponseEntity<ApiResult<AddendumContentDTO>> getContent(@PathVariable UUID publicId) {
        log.info("REST request to get content for Addendum: {}", publicId);
        AddendumContentDTO response = addendumUseCase.getContentByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener todas las adendas paginadas y filtradas")
    @GetMapping
    public ResponseEntity<ApiResult<PagedResult<AddendumListDTO>>> getAll(
            @Valid @ModelAttribute AddendumPageableRequest request) {
        log.info("REST request to get all Addendums with filters: {}", request);
        PagedResult<AddendumListDTO> result = addendumUseCase.findAllPaged(request.getAddendumNumber(), request.getContractPublicId(), request.toPageable());
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/states/select-options")
    @Operation(summary = "Obtener estados para campos de selección de adendas")
    public ResponseEntity<ApiResult<List<StateSelectOptionDTO>>> getStatesForSelect() {
        List<StateSelectOptionDTO> response = addendumUseCase.getStatesSelectOptions();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Generar URL de subida para un archivo de adenda")
    @PostMapping("/{publicId}/upload-url")
    public ResponseEntity<ApiResult<UploadUrlResponse>> generateUploadUrl(@PathVariable UUID publicId, @Valid @RequestBody GenerateUploadUrlRequest request) {
        log.info("REST request to generate upload URL for addendum {}: {}", publicId, request);
        UploadUrlResponse response = addendumUseCase.generateUploadUrl(request);
        return ResponseEntity.ok(ApiResult.success(response, "URL de subida generada exitosamente."));
    }

    @Operation(summary = "Adjuntar archivo a una adenda")
    @PostMapping("/{publicId}/attach-file")
    public ResponseEntity<ApiResult<CommandAddendumResponse>> attachFile(@PathVariable UUID publicId, @Valid @RequestBody AttachFileRequest request) {
        log.info("REST request to attach file to addendum {}: {}", publicId, request);
        CommandAddendumResponse response = addendumUseCase.attachFile(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response, "Archivo adjuntado exitosamente."));
    }

    @Operation(summary = "Firmar una adenda")
    @PostMapping("/{publicId}/sign")
    public ResponseEntity<ApiResult<CommandAddendumResponse>> sign(@PathVariable UUID publicId, @Valid @RequestBody SignAddendumRequest request) {
        log.info("REST request to sign addendum {}", publicId);
        CommandAddendumResponse response = addendumUseCase.signAddendum(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response, "Adenda firmada exitosamente."));
    }
}