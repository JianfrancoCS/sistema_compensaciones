package com.agropay.core.organization.web;

import com.agropay.core.organization.application.services.SubsidiarySignerService;
import com.agropay.core.organization.model.signer.CreateSubsidiarySignerRequest;
import com.agropay.core.organization.model.signer.SubsidiarySignerDetailsDTO;
import com.agropay.core.organization.model.signer.SubsidiarySignerListDTO;
import com.agropay.core.organization.model.signer.SubsidiarySignerPageableRequest;
import com.agropay.core.organization.model.signer.UpdateSubsidiarySignerRequest;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/subsidiary-signers")
@RequiredArgsConstructor
@Tag(name = "Gestión de Responsables de Firma", description = "Operaciones para gestionar los responsables de firma de boletas de pago por subsidiaria.")
public class SubsidiarySignerController {

    private final SubsidiarySignerService signerService;

    @GetMapping
    @Operation(summary = "Listar subsidiarias con responsables de firma (paginado)", 
               description = "Obtiene una lista paginada de subsidiarias con información sobre su responsable de firma asignado.")
    public ResponseEntity<ApiResult<PagedResult<SubsidiarySignerListDTO>>> listSubsidiariesWithSignersPaged(
            @Valid SubsidiarySignerPageableRequest request) {
        PagedResult<SubsidiarySignerListDTO> response = signerService.listSubsidiariesWithSignersPaged(
            request.getSubsidiaryName(),
            request.getResponsibleEmployeeName(),
            request.toPageable()
        );
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas las subsidiarias con responsables de firma (sin paginación)", 
               description = "Obtiene una lista completa de todas las subsidiarias con información sobre su responsable de firma asignado.")
    public ResponseEntity<ApiResult<List<SubsidiarySignerListDTO>>> listSubsidiariesWithSigners() {
        List<SubsidiarySignerListDTO> response = signerService.listSubsidiariesWithSigners();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/subsidiary/{subsidiaryPublicId}")
    @Operation(summary = "Obtener responsable de firma de una subsidiaria", 
               description = "Obtiene los detalles del responsable de firma asignado a una subsidiaria específica.")
    public ResponseEntity<ApiResult<SubsidiarySignerDetailsDTO>> getSignerBySubsidiary(
            @PathVariable UUID subsidiaryPublicId) {
        SubsidiarySignerDetailsDTO response = signerService.getSignerBySubsidiary(subsidiaryPublicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PostMapping
    @Operation(summary = "Asignar responsable de firma", 
               description = "Asigna un nuevo responsable de firma para una subsidiaria. Crea un registro histórico (no actualiza el existente).")
    public ResponseEntity<ApiResult<SubsidiarySignerDetailsDTO>> createSigner(
            @RequestPart("request") @Valid CreateSubsidiarySignerRequest request,
            @RequestPart(value = "signatureImage", required = false) MultipartFile signatureImage) {
        SubsidiarySignerDetailsDTO response = signerService.createOrUpdateSigner(request, signatureImage);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar responsable de firma", 
               description = "Actualiza un responsable de firma existente. Crea un nuevo registro histórico con los datos actualizados.")
    public ResponseEntity<ApiResult<SubsidiarySignerDetailsDTO>> updateSigner(
            @PathVariable UUID publicId,
            @RequestPart("request") @Valid UpdateSubsidiarySignerRequest request,
            @RequestPart(value = "signatureImage", required = false) MultipartFile signatureImage) {
        SubsidiarySignerDetailsDTO response = signerService.updateSigner(publicId, request, signatureImage);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/subsidiary/{subsidiaryPublicId}")
    @Operation(summary = "Eliminar responsable de firma", 
               description = "Elimina el responsable de firma más reciente de una subsidiaria (soft delete).")
    public ResponseEntity<ApiResult<Void>> deleteSigner(@PathVariable UUID subsidiaryPublicId) {
        signerService.deleteSigner(subsidiaryPublicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }
}

