package com.agropay.core.assignment.web;

import com.agropay.core.assignment.application.usecase.ITareoUseCase;
import com.agropay.core.assignment.domain.TareoEntity;
import com.agropay.core.assignment.model.tareo.*;
import com.agropay.core.assignment.persistence.ITareoEmployeeRepository;
import com.agropay.core.assignment.persistence.IQrRollEmployeeRepository;
import com.agropay.core.assignment.persistence.IHarvestRecordRepository;
import com.agropay.core.shared.batch.BatchResponse;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(TareoController.BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Tareos", description = "Endpoints para gestionar tareos y registro de asistencia")
public class TareoController {

    public static final String BASE_URL = "/v1/tareos";

    private final ITareoUseCase tareoUseCase;
    private final ITareoEmployeeRepository tareoEmployeeRepository;
    private final IQrRollEmployeeRepository qrRollEmployeeRepository;
    private final IHarvestRecordRepository harvestRecordRepository;

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar un tareo (solo si no tiene empleados registrados)")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable UUID publicId) {
        tareoUseCase.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping
    @Operation(summary = "Obtener lista paginada de tareos con filtros")
    public ResponseEntity<ApiResult<PagedResult<TareoListDTO>>> findAllPaged(
            @Valid TareoPageableRequest pageableRequest) {
        Pageable pageable = pageableRequest.toPageable();
        PagedResult<TareoListDTO> result = tareoUseCase.findAllPaged(
                pageableRequest.getLaborPublicId(),
                pageableRequest.getSubsidiaryPublicId(),
                pageableRequest.getCreatedBy(),
                pageableRequest.getDateFrom(),
                pageableRequest.getDateTo(),
                pageableRequest.getIsProcessed(),
                pageable
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @PostMapping("/batch-sync")
    @Operation(summary = "Sincronización batch de tareos con empleados (offline-first)")
    public ResponseEntity<ApiResult<BatchResponse<BatchTareoResultData>>> batchSync(
            @Valid @RequestBody BatchTareoSyncRequest request) {
        BatchResponse<BatchTareoResultData> response = tareoUseCase.batchSync(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/daily")
    @Operation(summary = "Obtener lista paginada de tareos día a día con indicador de calculado")
    public ResponseEntity<ApiResult<PagedResult<TareoDailyDTO>>> findAllDailyPaged(
            @Valid TareoDailyPageableRequest pageableRequest) {
        Pageable pageable = pageableRequest.toPageable();
        PagedResult<TareoDailyDTO> result = tareoUseCase.findAllDailyPaged(
                pageableRequest.getLaborPublicId(),
                pageableRequest.getSubsidiaryPublicId(),
                pageableRequest.getDateFrom(),
                pageableRequest.getDateTo(),
                pageableRequest.getIsCalculated(),
                pageable
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Obtener detalle completo de un tareo")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResult<TareoDetailDTO>> getDetail(@PathVariable UUID publicId) {
        TareoEntity t = tareoUseCase.findByPublicId(publicId);

        TareoDetailDTO.LaborInfo laborInfo = new TareoDetailDTO.LaborInfo(
                t.getLabor().getName(),
                Boolean.TRUE.equals(t.getLabor().getIsPiecework())
        );

        // Manejar caso cuando lote es null (tareos administrativos)
        String loteName = t.getLote() != null ? t.getLote().getName() : null;
        String subsidiaryName = t.getSubsidiary() != null ? t.getSubsidiary().getName() : null;
        TareoDetailDTO.LoteInfo loteInfo = new TareoDetailDTO.LoteInfo(
                loteName,
                subsidiaryName
        );

        TareoDetailDTO.SupervisorInfo supervisorInfo = new TareoDetailDTO.SupervisorInfo(
                t.getSupervisor().getPersonDocumentNumber(),
                t.getSupervisor().getPerson() != null ? t.getSupervisor().getPerson().getNames() : null
        );

        TareoDetailDTO.AcopiadorInfo acopiadorInfo = null;
        if (t.getScanner() != null) {
            acopiadorInfo = new TareoDetailDTO.AcopiadorInfo(
                t.getScanner().getPersonDocumentNumber(),
                t.getScanner().getPerson() != null ? t.getScanner().getPerson().getNames() : null
            );
        }

        LocalDate tareoDate = t.getCreatedAt().toLocalDate();
        boolean isPiecework = Boolean.TRUE.equals(t.getLabor().getIsPiecework());
        BigDecimal minTaskRequirement = t.getLabor().getMinTaskRequirement();
        
        var employeeItems = tareoEmployeeRepository.findAllByTareoId(t.getId()).stream()
                .map(te -> {
                    TareoDetailDTO.ProductivityInfo productivityInfo = null;
                    
                    // Solo calcular productividad si la labor es de destajo
                    if (isPiecework && minTaskRequirement != null && minTaskRequirement.compareTo(BigDecimal.ZERO) > 0) {
                        // Usar el campo productivity de tareo_employees si está disponible (ya calculado al cerrar el tareo)
                        // Si no está disponible, calcular desde harvest_records (fallback para tareos antiguos)
                        Integer productivityCount = te.getProductivity();
                        Long harvestCount = null;
                        
                        if (productivityCount != null) {
                            // Usar el valor ya calculado y guardado
                            harvestCount = productivityCount.longValue();
                        } else {
                            // Fallback: calcular desde harvest_records
                            Optional<com.agropay.core.assignment.domain.QrRollEmployeeEntity> qrRollAssignment = 
                                qrRollEmployeeRepository.findByEmployeeCodeAndDate(te.getEmployee().getCode(), tareoDate);
                            
                            if (qrRollAssignment.isPresent()) {
                                Integer qrRollId = qrRollAssignment.get().getQrRoll().getId();
                                harvestCount = harvestRecordRepository.countByQrRollId(qrRollId);
                            }
                        }
                        
                        // Si hay harvestCount (ya sea del campo o calculado), crear ProductivityInfo
                        if (harvestCount != null) {
                            // Calcular productividad: (harvestCount / minTaskRequirement) * 100
                            BigDecimal productivityPercentage = BigDecimal.valueOf(harvestCount)
                                .divide(minTaskRequirement, 2, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                            
                            // Obtener la unidad de medida de la labor
                            String unitOfMeasure = t.getLabor().getLaborUnit() != null 
                                ? t.getLabor().getLaborUnit().getName() 
                                : null;
                            
                            // El acopiador es el empleado que tiene el QrRoll asignado (el mismo empleado en este caso)
                            TareoDetailDTO.CollectorInfo collectorInfo = new TareoDetailDTO.CollectorInfo(
                                te.getEmployee().getPersonDocumentNumber(),
                                te.getEmployee().getPerson() != null ? te.getEmployee().getPerson().getNames() : null
                            );
                            
                            productivityInfo = new TareoDetailDTO.ProductivityInfo(
                                productivityPercentage,
                                harvestCount,
                                minTaskRequirement,
                                unitOfMeasure,
                                collectorInfo
                            );
                        }
                        // Si no hay harvestCount y no es de destajo, productivityInfo queda null (correcto)
                    }
                    // Si la labor NO es de destajo, productivityInfo queda null (correcto)
                    
                    return new TareoDetailDTO.EmployeeItem(
                        te.getPublicId(),
                        te.getEmployee().getPersonDocumentNumber(),
                        te.getEmployee().getPerson() != null ? te.getEmployee().getPerson().getNames() : null,
                        te.getEmployee().getPosition() != null ? te.getEmployee().getPosition().getName() : null,
                        te.getStartTime(),
                        te.getEndTime(),
                        productivityInfo
                    );
                })
                .collect(Collectors.toList());

        TareoDetailDTO dto = new TareoDetailDTO(
                t.getPublicId(),
                laborInfo,
                loteInfo,
                supervisorInfo,
                acopiadorInfo,
                t.getCreatedBy(),
                t.getCreatedAt(),
                employeeItems
        );

        return ResponseEntity.ok(ApiResult.success(dto));
    }

    @GetMapping("/{publicId}/employees/sync")
    @Operation(summary = "Obtener empleados de un tareo con QR rolls para sincronización móvil")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResult<java.util.List<EmployeeSyncResponse>>> getEmployeesForSync(
            @PathVariable UUID publicId) {
        java.util.List<EmployeeSyncResponse> employees = tareoUseCase.getEmployeesForSync(publicId);
        return ResponseEntity.ok(ApiResult.success(employees));
    }

}
