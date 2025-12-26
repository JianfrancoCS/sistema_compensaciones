package com.agropay.core.assignment.web;

import com.agropay.core.assignment.application.usecase.IQrRollUseCase;
import com.agropay.core.assignment.model.qrroll.*;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(QrRollController.BASE_URL)
@RequiredArgsConstructor
@Tag(name = "QR Rolls", description = "Endpoints para gestionar rollos de códigos QR")
public class QrRollController {

    public static final String BASE_URL = "/v1/qr-rolls";

    private final IQrRollUseCase qrRollUseCase;

    @PostMapping
    @Operation(summary = "Crear un rollo de QR")
    public ResponseEntity<ApiResult<CommandQrRollResponse>> createRoll(@Valid @RequestBody CreateQrRollRequest request) {
        CommandQrRollResponse response = qrRollUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(response));
    }

    @PutMapping("/{rollPublicId}")
    @Operation(summary = "Actualizar configuración de un rollo de QR")
    public ResponseEntity<ApiResult<CommandQrRollResponse>> updateRoll(
            @PathVariable UUID rollPublicId,
            @Valid @RequestBody UpdateQrRollRequest request) {
        CommandQrRollResponse response = qrRollUseCase.update(rollPublicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{rollPublicId}")
    @Operation(summary = "Eliminar un rollo de QR (soft delete)")
    public ResponseEntity<ApiResult<Void>> deleteRoll(@PathVariable UUID rollPublicId) {
        qrRollUseCase.delete(rollPublicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @PostMapping("/{rollPublicId}/generate-codes")
    @Operation(summary = "Generar códigos QR para un rollo")
    public ResponseEntity<ApiResult<Void>> generateQrCodes(
            @PathVariable UUID rollPublicId,
            @Valid @RequestBody GenerateQrCodesRequest request) {
        qrRollUseCase.generateQrCodes(rollPublicId, request.quantity());
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @PostMapping("/{rollPublicId}/print")
    @Operation(summary = "Marcar los códigos QR de hoy como impresos")
    public ResponseEntity<ApiResult<Void>> printQrCodes(@PathVariable UUID rollPublicId) {
        qrRollUseCase.printQrCodes(rollPublicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @PostMapping("/assign")
    @Operation(summary = "Asignar rollo completo a un empleado para el día actual")
    public ResponseEntity<ApiResult<Void>> assignRollToEmployee(@Valid @RequestBody AssignRollToEmployeeRequest request) {
        qrRollUseCase.assignToEmployee(request.rollPublicId(), request.employeeDocumentNumber());
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @PostMapping("/batch-assign")
    @Operation(summary = "Asignar múltiples rollos a empleados en batch (offline-first)")
    public ResponseEntity<ApiResult<com.agropay.core.shared.batch.BatchResponse<Void>>> batchAssign(
            @Valid @RequestBody BatchAssignQrRollsRequest request) {
        com.agropay.core.shared.batch.BatchResponse<Void> response = qrRollUseCase.batchAssign(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Listar todos los rollos paginados")
    public ResponseEntity<ApiResult<PagedResult<QrRollListDTO>>> findAllPaged(@Valid QrRollPageableRequest request) {
        PagedResult<QrRollListDTO> result = qrRollUseCase.findAllPaged(request);
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/print-stats")
    @Operation(summary = "Obtener estadísticas de impresión de códigos QR")
    public ResponseEntity<ApiResult<PagedResult<PrintStatsDTO>>> findPrintStats(@Valid PrintStatsPageableRequest request) {
        PagedResult<PrintStatsDTO> result = qrRollUseCase.findPrintStats(request);
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/available")
    @Operation(summary = "Obtener estadísticas de rollos disponibles y en uso para una fecha específica")
    public ResponseEntity<ApiResult<AvailableQrRollsStatsResponse>> findAvailableRollsStats(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date) {
        AvailableQrRollsStatsResponse stats = qrRollUseCase.findAvailableRollsStats(date);
        return ResponseEntity.ok(ApiResult.success(stats));
    }

    @GetMapping("/{rollPublicId}/qr-codes")
    @Operation(summary = "Obtener códigos QR de un rollo con filtros opcionales para el día actual")
    public ResponseEntity<ApiResult<List<QrCodeDTO>>> findQrCodesByRoll(
            @PathVariable UUID rollPublicId,
            @RequestParam(required = false) Boolean isUsed,
            @RequestParam(required = false) Boolean isPrinted) {
        List<QrCodeDTO> qrCodes = qrRollUseCase.findQrCodesByRoll(rollPublicId, isUsed, isPrinted, LocalDate.now());
        return ResponseEntity.ok(ApiResult.success(qrCodes));
    }

    @PostMapping("/batch-generate")
    @Operation(summary = "Generar códigos QR en batch para múltiples rollos disponibles")
    public ResponseEntity<ApiResult<Void>> batchGenerateQrCodes(@Valid @RequestBody BatchGenerateQrCodesRequest request) {
        qrRollUseCase.batchGenerateQrCodes(request.rollsNeeded(), request.codesPerRoll());
        return ResponseEntity.ok(ApiResult.success(null));
    }
}