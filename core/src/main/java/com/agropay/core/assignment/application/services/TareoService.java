package com.agropay.core.assignment.application.services;

import com.agropay.core.assignment.application.usecase.ILaborUseCase;
import com.agropay.core.assignment.application.usecase.ILoteUseCase;
import com.agropay.core.assignment.application.usecase.ITareoMotiveUseCase;
import com.agropay.core.assignment.application.usecase.ITareoUseCase;
import com.agropay.core.assignment.domain.LaborEntity;
import com.agropay.core.assignment.domain.LoteEntity;
import com.agropay.core.assignment.domain.TareoEmployeeEntity;
import com.agropay.core.assignment.domain.TareoEmployeeMotiveEntity;
import com.agropay.core.assignment.domain.TareoEntity;
import com.agropay.core.assignment.domain.TareoMotiveEntity;
import com.agropay.core.assignment.domain.QrRollEmployeeEntity;
import com.agropay.core.assignment.domain.QrCodeEntity;
import com.agropay.core.assignment.mapper.ITareoMapper;
import com.agropay.core.assignment.model.tareo.*;
import com.agropay.core.assignment.persistence.ITareoEmployeeMotiveRepository;
import com.agropay.core.assignment.persistence.ITareoEmployeeRepository;
import com.agropay.core.assignment.persistence.ITareoRepository;
import com.agropay.core.assignment.persistence.TareoSpecification;
import com.agropay.core.assignment.persistence.IQrRollEmployeeRepository;
import com.agropay.core.assignment.persistence.IQrCodeRepository;
import com.agropay.core.organization.application.usecase.IEmployeeUseCase;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.payroll.persistence.IPayrollDetailRepository;
import com.agropay.core.shared.batch.BatchItemResult;
import com.agropay.core.shared.batch.BatchResponse;
import com.agropay.core.shared.batch.BatchSummary;
import com.agropay.core.shared.batch.ErrorDetail;
import com.agropay.core.shared.batch.ResultStatus;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TareoService implements ITareoUseCase {

    private final ITareoRepository tareoRepository;
    private final ITareoEmployeeRepository tareoEmployeeRepository;
    private final ITareoEmployeeMotiveRepository tareoEmployeeMotiveRepository;
    private final ITareoMapper tareoMapper;
    private final ILaborUseCase laborUseCase;
    private final ILoteUseCase loteUseCase;
    private final IEmployeeUseCase employeeUseCase;
    private final ITareoMotiveUseCase tareoMotiveUseCase;
    private final IQrRollEmployeeRepository qrRollEmployeeRepository;
    private final IQrCodeRepository qrCodeRepository;
    private final IPayrollDetailRepository payrollDetailRepository;
    private final com.agropay.core.assignment.persistence.IHarvestRecordRepository harvestRecordRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete tareo with public ID: {}", publicId);
        TareoEntity tareoToDelete = findByPublicId(publicId);

        long tareoEmployeeCount = tareoRepository.countTareoEmployeesByTareoId(tareoToDelete.getId());
        if (tareoEmployeeCount > 0) {
            log.warn("Attempted to delete tareo {} which still has {} associated employee(s).", publicId, tareoEmployeeCount);
            throw new ReferentialIntegrityException("exception.assignment.tareo.cannot-delete-has-employees", tareoEmployeeCount);
        }

        tareoRepository.softDelete(tareoToDelete.getId(), "SYSTEM");
        log.info("Successfully deleted tareo with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<TareoListDTO> findAllPaged(UUID laborPublicId, UUID subsidiaryPublicId, String createdBy,
                                                   LocalDate dateFrom, LocalDate dateTo, Boolean isProcessed, Pageable pageable) {
        log.info("Fetching paged list of tareos with filters - laborPublicId: {}, subsidiaryPublicId: {}, createdBy: '{}', dateFrom: {}, dateTo: {}, isProcessed: {}, page: {}, size: {}",
                laborPublicId, subsidiaryPublicId, createdBy, dateFrom, dateTo, isProcessed, pageable.getPageNumber(), pageable.getPageSize());

        Specification<TareoEntity> spec = TareoSpecification.filterBy(laborPublicId, subsidiaryPublicId, createdBy, dateFrom, dateTo, isProcessed);
        Page<TareoEntity> tareoPage = tareoRepository.findAll(spec, pageable);

        Page<TareoListDTO> enrichedPage = tareoPage.map(tareo -> {
            long employeeCount = tareoRepository.countTareoEmployeesByTareoId(tareo.getId());
            Boolean     tareoProcessedStatus = payrollDetailRepository.isTareoIdCalculated(tareo.getId());
            boolean tareoIsProcessed = Boolean.TRUE.equals(tareoProcessedStatus);
            
            // Obtener información del lote (puede ser null para tareos administrativos)
            String loteName = tareo.getLote() != null ? tareo.getLote().getName() : null;
            
            // Obtener subsidiaria directamente del tareo (relación directa)
            String subsidiaryName = tareo.getSubsidiary() != null ? tareo.getSubsidiary().getName() : null;
            
            return new TareoListDTO(
                tareo.getPublicId(),
                tareo.getLabor() != null ? tareo.getLabor().getName() : null,
                loteName,
                subsidiaryName,
                employeeCount,
                tareoIsProcessed,
                tareo.getCreatedAt()
            );
        });

        return new PagedResult<>(enrichedPage);
    }

    @Override
    @Transactional
    public BatchResponse<BatchTareoResultData> batchSync(BatchTareoSyncRequest request) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🔄 BATCH SYNC INICIADO");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("Total tareos a procesar: {}", request.tareos().size());

        List<BatchItemResult<BatchTareoResultData>> results = new ArrayList<>();

        for (int i = 0; i < request.tareos().size(); i++) {
            BatchTareoData tareoData = request.tareos().get(i);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📋 Procesando tareo {}/{}: temporalId={}", 
                    i + 1, request.tareos().size(), tareoData.temporalId());
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            try {
                log.info("🔄 Iniciando procesamiento de tareo {}", tareoData.temporalId());
                BatchItemResult<BatchTareoResultData> result = processSingleTareo(tareoData);
                results.add(result);
                
                log.info("📊 Resultado del tareo {}: Status={}, Success={}, HasErrors={}", 
                        tareoData.temporalId(), 
                        result.status(), 
                        result.success(),
                        !result.errors().isEmpty());
                
                if (result.isSuccess()) {
                    log.info("✅ Tareo {} procesado exitosamente (SUCCESS)", tareoData.temporalId());
                } else if (result.status() == ResultStatus.PARTIAL_SUCCESS) {
                    log.warn("⚠️ Tareo {} procesado parcialmente (PARTIAL_SUCCESS) - {} error(es)", 
                            tareoData.temporalId(), result.errors().size());
                    result.errors().forEach(error -> 
                        log.warn("  - Error: [{}] {} - {}", 
                                error.errorCode(), error.identifier(), error.message()));
                } else {
                    log.error("❌ Tareo {} falló completamente (ERROR) - {} error(es)", 
                            tareoData.temporalId(), result.errors().size());
                    result.errors().forEach(error -> 
                        log.error("  - Error: [{}] {} - {}", 
                                error.errorCode(), error.identifier(), error.message()));
                }
            } catch (Exception e) {
                log.error("❌ Error inesperado procesando tareo {}: {} - {}", 
                        tareoData.temporalId(), e.getClass().getSimpleName(), e.getMessage(), e);
                results.add(BatchItemResult.error(
                        tareoData.temporalId(),
                        "UNEXPECTED_ERROR",
                        "Unexpected error: " + e.getMessage()
                ));
            }
        }

        long successful = results.stream().filter(BatchItemResult::isSuccess).count();
        long failed = results.stream().filter(BatchItemResult::isError).count();
        long partialSuccess = results.stream()
                .filter(r -> r.status() == ResultStatus.PARTIAL_SUCCESS)
                .count();
        
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🏁 BATCH SYNC COMPLETADO");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("Total tareos: {}", results.size());
        log.info("  ✅ Exitosos (SUCCESS): {}", successful);
        log.info("  ⚠️  Parciales (PARTIAL_SUCCESS): {}", partialSuccess);
        log.info("  ❌ Fallidos (ERROR): {}", failed);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Log detallado de cada resultado
        for (int i = 0; i < results.size(); i++) {
            BatchItemResult<BatchTareoResultData> result = results.get(i);
            log.info("Tareo {}: Status={}, Success={}, Errors={}", 
                    result.identifier(), 
                    result.status(), 
                    result.success(),
                    result.errors().size());
            if (!result.errors().isEmpty()) {
                result.errors().forEach(error -> 
                    log.info("  - Error: [{}] {} - {}", 
                            error.errorCode(), error.identifier(), error.message()));
            }
        }
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return BatchResponse.of(results);
    }

    private BatchItemResult<BatchTareoResultData> processSingleTareo(BatchTareoData tareoData) {
        log.info("Processing tareo with temporalId: {}", tareoData.temporalId());

        LaborEntity labor;
        LoteEntity lote = null; 
        EmployeeEntity supervisor;
        EmployeeEntity scanner = null;

        try {
            labor = laborUseCase.findByPublicId(tareoData.laborPublicId());
        } catch (IdentifierNotFoundException e) {
            return BatchItemResult.error(tareoData.temporalId(), "LABOR_NOT_FOUND", "Labor does not exist");
        }

        if (tareoData.lotePublicId() != null) {
            try {
                lote = loteUseCase.findByPublicId(tareoData.lotePublicId());
            } catch (IdentifierNotFoundException e) {
                return BatchItemResult.error(tareoData.temporalId(), "LOTE_NOT_FOUND", "Lote does not exist");
            }
        } else {
            if (Boolean.TRUE.equals(labor.getIsPiecework())) {
                return BatchItemResult.error(
                    tareoData.temporalId(),
                    "LOTE_REQUIRED_FOR_PIECEWORK",
                    "Lote is required for piecework labors"
                );
            }
            log.info("Tareo administrativo sin lote: {}", tareoData.temporalId());
        }

        try {
            supervisor = employeeUseCase.findByDocumentNumber(tareoData.supervisorDocumentNumber());
        } catch (IdentifierNotFoundException e) {
            return BatchItemResult.error(tareoData.temporalId(), "SUPERVISOR_NOT_FOUND",
                    "Supervisor with document number " + tareoData.supervisorDocumentNumber() + " does not exist");
        }

        if (tareoData.scannerDocumentNumber() != null && !tareoData.scannerDocumentNumber().isBlank()) {
            try {
                scanner = employeeUseCase.findByDocumentNumber(tareoData.scannerDocumentNumber());
            } catch (IdentifierNotFoundException e) {
                log.warn("Scanner with document number {} not found for tareo {}",
                        tareoData.scannerDocumentNumber(), tareoData.temporalId());
            }
        }

        if (Boolean.TRUE.equals(labor.getIsPiecework())) {
            if (scanner == null) {
                return BatchItemResult.error(
                        tareoData.temporalId(),
                        "SCANNER_REQUIRED_FOR_PIECEWORK",
                        "Scanner (pedeteador) is required for piecework labors"
                );
            }
            // Validar que el scanner pertenezca a la misma subsidiary del lote (solo si hay lote)
            if (lote != null && (scanner.getSubsidiary() == null || !scanner.getSubsidiary().getPublicId().equals(lote.getSubsidiary().getPublicId()))) {
                return BatchItemResult.error(
                        tareoData.temporalId(),
                        "SCANNER_SUBSIDIARY_MISMATCH",
                        "Scanner must belong to the same subsidiary as the lote"
                );
            }
        }

        TareoEntity tareo = tareoRepository.findByTemporalIdAndDeletedAtIsNull(tareoData.temporalId())
                .orElse(null);

        boolean isNewTareo = (tareo == null);

        if (isNewTareo) {
            tareo = new TareoEntity();
            tareo.setTemporalId(tareoData.temporalId());
            log.info("Creating new tareo with temporalId: {}", tareoData.temporalId());
        } else {
            log.info("Updating existing tareo with temporalId: {}, publicId: {}", tareoData.temporalId(), tareo.getPublicId());
        }

        tareo.setLabor(labor);
        tareo.setLote(lote);
        tareo.setSupervisor(supervisor);
        tareo.setScanner(scanner);
        
        // Establecer subsidiaria: del lote si existe, sino del supervisor
        if (lote != null && lote.getSubsidiary() != null) {
            tareo.setSubsidiary(lote.getSubsidiary());
        } else if (supervisor.getSubsidiary() != null) {
            tareo.setSubsidiary(supervisor.getSubsidiary());
        } else {
            return BatchItemResult.error(
                    tareoData.temporalId(),
                    "SUBSIDIARY_REQUIRED",
                    "Subsidiary is required but could not be determined from lote or supervisor"
            );
        }
        
        if (Boolean.TRUE.equals(tareoData.isClosing())) {
            tareo.setClosedAt(java.time.LocalDateTime.now());
            log.info("Marcando tareo {} como cerrado", tareoData.temporalId());
            
            // Validar motivo de cierre si se proporciona (se aplicará a empleados sin salida)
            if (tareoData.closingMotivePublicId() != null) {
                log.debug("Validando motivo de cierre con publicId: {}", tareoData.closingMotivePublicId());
                try {
                    TareoMotiveEntity closingMotive = tareoMotiveUseCase.findByPublicId(tareoData.closingMotivePublicId());
                    log.info("Motivo de cierre válido: {} (ID: {})", closingMotive.getName(), closingMotive.getId());
                } catch (IdentifierNotFoundException e) {
                    log.warn("Motivo de cierre con publicId {} no encontrado para tareo {}", 
                            tareoData.closingMotivePublicId(), tareoData.temporalId());
                    return BatchItemResult.error(
                            tareoData.temporalId(),
                            "CLOSING_MOTIVE_NOT_FOUND",
                            "El motivo de cierre especificado no existe"
                    );
                }
            } else {
                log.warn("Tareo {} marcado como cerrado pero no se proporcionó motivo de cierre", tareoData.temporalId());
                return BatchItemResult.error(
                        tareoData.temporalId(),
                        "CLOSING_MOTIVE_REQUIRED",
                        "El motivo de cierre es requerido cuando se cierra un tareo"
                );
            }
        }

        TareoEntity savedTareo = tareoRepository.save(tareo);

        // Calcular delta de empleados si es update
        List<String> employeesToInsert;
        List<String> employeesToDelete = new ArrayList<>();

        if (!isNewTareo) {
            // Obtener empleados actuales en BD
            List<String> currentEmployeeDocs = tareoEmployeeRepository.findEmployeeDocumentNumbersByTareoId(savedTareo.getId());

            // Empleados que vienen del móvil (estado completo)
            List<String> receivedEmployeeDocs = tareoData.employees().stream()
                    .map(BatchEmployeeData::documentNumber)
                    .toList();

            // Calcular delta
            employeesToInsert = receivedEmployeeDocs.stream()
                    .filter(doc -> !currentEmployeeDocs.contains(doc))
                    .toList();

            employeesToDelete = currentEmployeeDocs.stream()
                    .filter(doc -> !receivedEmployeeDocs.contains(doc))
                    .toList();

            log.info("Delta calculation - toInsert: {}, toDelete: {}", employeesToInsert.size(), employeesToDelete.size());

            // Soft delete empleados removidos
            if (!employeesToDelete.isEmpty()) {
                tareoEmployeeRepository.softDeleteByTareoIdAndDocumentNumbers(savedTareo.getId(), employeesToDelete);
                log.info("Soft deleted {} employees from tareo {}", employeesToDelete.size(), savedTareo.getTemporalId());
            }
        } else {
            // Tareo nuevo: insertar todos
            employeesToInsert = tareoData.employees().stream()
                    .map(BatchEmployeeData::documentNumber)
                    .toList();
        }

        // Procesar solo empleados a insertar
        List<ErrorDetail> employeeErrors = new ArrayList<>();
        int successfulEmployees = 0;
        int failedEmployees = 0;
        int totalEmployeesToProcess = employeesToInsert.size();

        LocalDate tareoDate = savedTareo.getCreatedAt().toLocalDate();

        log.info("Processing {} employees for tareo {} ({} to insert)", 
                tareoData.employees().size(), tareoData.temporalId(), totalEmployeesToProcess);
        for (BatchEmployeeData empData : tareoData.employees()) {
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("Processing employee: DNI={}, EntryTime={}, ExitTime={}", 
                    empData.documentNumber(), empData.entryTime(), empData.exitTime());
            log.info("EntryMotivePublicId={}, ExitMotivePublicId={}", 
                    empData.entryMotivePublicId(), empData.exitMotivePublicId());
            
            // Solo procesar si está en la lista de inserción
            if (!employeesToInsert.contains(empData.documentNumber())) {
                log.debug("Employee {} already exists in tareo, skipping", empData.documentNumber());
                continue; // Ya existe, no hacer nada
            }

            try {
                log.debug("Finding employee with document number: {}", empData.documentNumber());
                EmployeeEntity employee = employeeUseCase.findByDocumentNumber(empData.documentNumber());
                log.info("Employee found: {} (Code: {})", empData.documentNumber(), employee.getCode());

                // Validar que el empleado no esté en otra labor el mismo día
                // Un empleado solo puede estar en UNA labor por día
                // NOTA: La validación ya filtra por deletedAt IS NULL (tanto tareo como tareo_employee)
                // Si un tareo tiene soft delete, NO se considerará en esta validación
                if (tareoEmployeeRepository.existsByEmployeeCodeAndDateInDifferentLabor(
                        employee.getCode(), tareoDate, labor.getId())) {
                    log.warn("⚠️ Empleado {} ya está registrado en otra labor el día {} (excluyendo tareos eliminados)", 
                            empData.documentNumber(), tareoDate);
                    
                    // Buscar el tareo existente donde el empleado ya está registrado
                    // Usar el método del repositorio que ya filtra por deletedAt
                    List<TareoEmployeeEntity> existingEmployees = tareoEmployeeRepository.findByEmployeeAndPeriod(
                            empData.documentNumber(), tareoDate, tareoDate);
                    
                    // Filtrar para encontrar el que está en diferente labor y no es el tareo actual
                    TareoEmployeeEntity existingTareoEmployee = existingEmployees.stream()
                            .filter(te -> !te.getTareo().getId().equals(savedTareo.getId()) &&
                                          !te.getTareo().getLabor().getId().equals(labor.getId()) &&
                                          te.getTareo().getDeletedAt() == null &&
                                          te.getDeletedAt() == null)
                            .findFirst()
                            .orElse(null);
                    
                    String existingLaborName = "otra labor";
                    if (existingTareoEmployee != null && existingTareoEmployee.getTareo().getLabor() != null) {
                        existingLaborName = existingTareoEmployee.getTareo().getLabor().getName();
                        log.info("   ✅ Labor existente encontrada: '{}' (tareo ID: {})", 
                                existingLaborName, existingTareoEmployee.getTareo().getId());
                    } else {
                        log.warn("   ⚠️ No se pudo encontrar el nombre de la labor existente");
                    }
                    
                    employeeErrors.add(ErrorDetail.of(
                            empData.documentNumber(),
                            "ALREADY_IN_DIFFERENT_LABOR",
                            String.format("El empleado %s ya está registrado en la labor '%s' el día %s. Un empleado solo puede estar en una labor por día.", 
                                empData.documentNumber(), existingLaborName, tareoDate)
                    ));
                    failedEmployees++;
                    log.warn("   ❌ Empleado {} rechazado - ya está en labor '{}'", 
                            empData.documentNumber(), existingLaborName);
                    continue;
                }
                
                // Validar si ya está en este mismo tareo (evitar duplicados)
                if (tareoEmployeeRepository.existsByEmployeeCodeAndTareoId(employee.getCode(), savedTareo.getId())) {
                    employeeErrors.add(ErrorDetail.of(
                            empData.documentNumber(),
                            "ALREADY_IN_THIS_TAREO",
                            "El empleado ya está registrado en este tareo"
                    ));
                    failedEmployees++;
                    continue;
                }

                // Crear TareoEmployee
                TareoEmployeeEntity tareoEmployee = new TareoEmployeeEntity();
                tareoEmployee.setTareo(savedTareo);
                tareoEmployee.setEmployee(employee);
                tareoEmployee.setStartTime(empData.entryTime());
                tareoEmployee.setEndTime(empData.exitTime());

                // Calcular actualHours (diferencia entre entrada y salida)
                if (empData.entryTime() != null && empData.exitTime() != null) {
                    java.time.Duration duration = java.time.Duration.between(empData.entryTime(), empData.exitTime());
                    // Si la salida es antes de la entrada, asumir que cruza medianoche (trabajo nocturno)
                    if (duration.isNegative()) {
                        duration = duration.plusDays(1);
                    }
                    BigDecimal actualHours = BigDecimal.valueOf(duration.toMinutes())
                            .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
                    tareoEmployee.setActualHours(actualHours);
                    log.debug("Calculated actualHours for employee {}: {} hours", empData.documentNumber(), actualHours);
                }

                log.debug("Saving TareoEmployeeEntity for employee {}", empData.documentNumber());
                TareoEmployeeEntity savedTareoEmployee = tareoEmployeeRepository.save(tareoEmployee);
                log.info("✓ TareoEmployeeEntity saved successfully for employee {} (ID: {})", 
                        empData.documentNumber(), savedTareoEmployee.getId());

                // Si es cierre de tareo y la labor es de destajo, calcular y guardar productividad
                if (Boolean.TRUE.equals(tareoData.isClosing()) && Boolean.TRUE.equals(labor.getIsPiecework())) {
                    try {
                        // Buscar el qr_roll_employee asignado a este empleado en la fecha del tareo
                        // (tareoDate ya está declarado arriba en la línea 272)
                        java.util.Optional<QrRollEmployeeEntity> qrRollEmployeeOpt = 
                            qrRollEmployeeRepository.findByEmployeeCodeAndDate(employee.getCode(), tareoDate);
                        
                        if (qrRollEmployeeOpt.isPresent()) {
                            QrRollEmployeeEntity qrRollEmployee = qrRollEmployeeOpt.get();
                            Integer qrRollId = qrRollEmployee.getQrRoll().getId();
                            
                            // Contar harvest_records del qr_roll asignado al empleado
                            Long harvestCount = harvestRecordRepository.countByQrRollIdForProductivity(qrRollId);
                            savedTareoEmployee.setProductivity(harvestCount != null ? harvestCount.intValue() : 0);
                            tareoEmployeeRepository.save(savedTareoEmployee);
                            log.info("Productividad calculada para empleado {} en tareo {}: {} unidades (qr_roll_id: {})", 
                                empData.documentNumber(), tareoData.temporalId(), savedTareoEmployee.getProductivity(), qrRollId);
                        } else {
                            log.warn("No se encontró qr_roll_employee para empleado {} en fecha {} - no se puede calcular productividad", 
                                empData.documentNumber(), tareoDate);
                            savedTareoEmployee.setProductivity(0);
                            tareoEmployeeRepository.save(savedTareoEmployee);
                        }
                    } catch (Exception e) {
                        log.warn("Error al calcular productividad para empleado {} en tareo {}: {}", 
                            empData.documentNumber(), tareoData.temporalId(), e.getMessage());
                        // En caso de error, establecer productividad en 0
                        savedTareoEmployee.setProductivity(0);
                        tareoEmployeeRepository.save(savedTareoEmployee);
                    }
                }

                // Guardar motivos si existen y determinar si son remunerados
                TareoMotiveEntity entryMotive = null;
                TareoMotiveEntity exitMotive = null;
                
                if (empData.entryMotivePublicId() != null) {
                    log.info("Processing entry motive for employee {}: motivePublicId={}", 
                            empData.documentNumber(), empData.entryMotivePublicId());
                    try {
                        log.debug("Finding entry motive with publicId: {}", empData.entryMotivePublicId());
                        entryMotive = tareoMotiveUseCase.findByPublicId(empData.entryMotivePublicId());
                        log.info("Entry motive found: {} (ID: {}, isPaid: {})", 
                                entryMotive.getName(), entryMotive.getId(), entryMotive.getIsPaid());
                        
                        log.debug("Creating TareoEmployeeMotiveEntity for entry");
                        TareoEmployeeMotiveEntity entryMotiveEntity = new TareoEmployeeMotiveEntity();
                        entryMotiveEntity.setTareoEmployee(savedTareoEmployee);
                        entryMotiveEntity.setMotive(entryMotive);
                        entryMotiveEntity.setAppliedAt(empData.entryTime());
                        
                        log.debug("Saving entry motive entity to database");
                        tareoEmployeeMotiveRepository.save(entryMotiveEntity);
                        log.info("✓ Entry motive saved successfully for employee {}", empData.documentNumber());
                    } catch (org.hibernate.exception.SQLGrammarException e) {
                        log.error("❌ SQL Grammar error saving entry motive for employee {}: {}", 
                                empData.documentNumber(), e.getMessage(), e);
                        log.error("SQL State: {}, Error Code: {}", e.getSQLState(), e.getErrorCode());
                        // Limpiar la sesión de Hibernate para evitar estados inconsistentes
                        entityManager.clear();
                        // No agregar error a employeeErrors para no fallar el tareo completo
                    } catch (org.springframework.dao.DataAccessException e) {
                        log.error("❌ Data access error saving entry motive for employee {}: {}", 
                                empData.documentNumber(), e.getMessage(), e);
                        // Limpiar la sesión de Hibernate para evitar estados inconsistentes
                        entityManager.clear();
                        // No agregar error a employeeErrors para no fallar el tareo completo
                    } catch (Exception e) {
                        log.error("❌ Unexpected error saving entry motive for employee {}: {} - {}", 
                                empData.documentNumber(), e.getClass().getSimpleName(), e.getMessage(), e);
                        // Limpiar la sesión de Hibernate para evitar estados inconsistentes
                        entityManager.clear();
                        // No agregar error a employeeErrors para no fallar el tareo completo
                    }
                } else {
                    log.debug("No entry motive provided for employee {}", empData.documentNumber());
                }

                if (empData.exitTime() != null && empData.exitMotivePublicId() != null) {
                    log.info("Processing exit motive for employee {}: motivePublicId={}", 
                            empData.documentNumber(), empData.exitMotivePublicId());
                    try {
                        log.debug("Finding exit motive with publicId: {}", empData.exitMotivePublicId());
                        exitMotive = tareoMotiveUseCase.findByPublicId(empData.exitMotivePublicId());
                        log.info("Exit motive found: {} (ID: {}, isPaid: {})", 
                                exitMotive.getName(), exitMotive.getId(), exitMotive.getIsPaid());
                        
                        log.debug("Creating TareoEmployeeMotiveEntity for exit");
                        TareoEmployeeMotiveEntity exitMotiveEntity = new TareoEmployeeMotiveEntity();
                        exitMotiveEntity.setTareoEmployee(savedTareoEmployee);
                        exitMotiveEntity.setMotive(exitMotive);
                        exitMotiveEntity.setAppliedAt(empData.exitTime());
                        
                        log.debug("Saving exit motive entity to database");
                        tareoEmployeeMotiveRepository.save(exitMotiveEntity);
                        log.info("✓ Exit motive saved successfully for employee {}", empData.documentNumber());
                    } catch (org.hibernate.exception.SQLGrammarException e) {
                        log.error("❌ SQL Grammar error saving exit motive for employee {}: {}", 
                                empData.documentNumber(), e.getMessage(), e);
                        log.error("SQL State: {}, Error Code: {}", e.getSQLState(), e.getErrorCode());
                        // Limpiar la sesión de Hibernate para evitar estados inconsistentes
                        entityManager.clear();
                        // No agregar error a employeeErrors para no fallar el tareo completo
                    } catch (org.springframework.dao.DataAccessException e) {
                        log.error("❌ Data access error saving exit motive for employee {}: {}", 
                                empData.documentNumber(), e.getMessage(), e);
                        // Limpiar la sesión de Hibernate para evitar estados inconsistentes
                        entityManager.clear();
                        // No agregar error a employeeErrors para no fallar el tareo completo
                    } catch (Exception e) {
                        log.error("❌ Unexpected error saving exit motive for employee {}: {} - {}", 
                                empData.documentNumber(), e.getClass().getSimpleName(), e.getMessage(), e);
                        // Limpiar la sesión de Hibernate para evitar estados inconsistentes
                        entityManager.clear();
                        // No agregar error a employeeErrors para no fallar el tareo completo
                    }
                } else {
                    log.debug("No exit motive provided for employee {} (exitTime: {}, exitMotivePublicId: {})", 
                            empData.documentNumber(), empData.exitTime(), empData.exitMotivePublicId());
                    
                    // Si es cierre de tareo y el empleado no tiene salida, aplicar motivo de cierre
                    if (Boolean.TRUE.equals(tareoData.isClosing()) && tareoData.closingMotivePublicId() != null) {
                        log.info("Aplicando motivo de cierre a empleado {} sin salida", empData.documentNumber());
                        try {
                            TareoMotiveEntity closingMotive = tareoMotiveUseCase.findByPublicId(tareoData.closingMotivePublicId());
                            log.info("Closing motive found: {} (ID: {}, isPaid: {})", 
                                    closingMotive.getName(), closingMotive.getId(), closingMotive.getIsPaid());
                            
                            // Establecer hora de salida si no tiene (usar hora actual del tareo o una hora por defecto)
                            LocalTime exitTime = empData.exitTime();
                            if (exitTime == null) {
                                // Usar hora actual o una hora por defecto (ej: 18:00)
                                exitTime = java.time.LocalTime.of(18, 0);
                                savedTareoEmployee.setEndTime(exitTime);
                                tareoEmployeeRepository.save(savedTareoEmployee);
                                log.debug("Establecida hora de salida por defecto: {}", exitTime);
                            }
                            
                            // Crear motivo de salida con el motivo de cierre
                            exitMotive = closingMotive;
                            TareoEmployeeMotiveEntity closingMotiveEntity = new TareoEmployeeMotiveEntity();
                            closingMotiveEntity.setTareoEmployee(savedTareoEmployee);
                            closingMotiveEntity.setMotive(closingMotive);
                            closingMotiveEntity.setAppliedAt(exitTime);
                            
                            tareoEmployeeMotiveRepository.save(closingMotiveEntity);
                            log.info("✓ Motivo de cierre aplicado a empleado {}: {} (hora: {}, isPaid: {})", 
                                    empData.documentNumber(), closingMotive.getName(), exitTime, closingMotive.getIsPaid());
                        } catch (IdentifierNotFoundException e) {
                            log.error("❌ Motivo de cierre no encontrado para empleado {}: {}", 
                                    empData.documentNumber(), e.getMessage());
                        } catch (Exception e) {
                            log.error("❌ Error al aplicar motivo de cierre a empleado {}: {}", 
                                    empData.documentNumber(), e.getMessage(), e);
                            // Limpiar la sesión de Hibernate para evitar estados inconsistentes
                            entityManager.clear();
                        }
                    }
                }

                // Calcular paidHours basándose en si los motivos son remunerados
                // Regla de negocio:
                // - Si ambos motivos (entrada y salida) son remunerados: paidHours = 8.0 (día completo)
                // - Si alguno no es remunerado: paidHours = actualHours (horas trabajadas)
                if (savedTareoEmployee.getActualHours() != null) {
                    boolean entryMotiveIsPaid = entryMotive != null && Boolean.TRUE.equals(entryMotive.getIsPaid());
                    boolean exitMotiveIsPaid = exitMotive != null && Boolean.TRUE.equals(exitMotive.getIsPaid());
                    
                    if (entryMotiveIsPaid && exitMotiveIsPaid) {
                        // Ambos motivos son remunerados: se asume día completo (8 horas)
                        // Independientemente de las horas reales trabajadas
                        BigDecimal fullDayHours = BigDecimal.valueOf(8.0);
                        savedTareoEmployee.setPaidHours(fullDayHours);
                        log.info("✓ Calculated paidHours for employee {}: {} hours (full day - both motives are paid, actualHours: {})", 
                                empData.documentNumber(), fullDayHours, savedTareoEmployee.getActualHours());
                    } else {
                        // Al menos un motivo no es remunerado: se pagan solo las horas trabajadas
                        savedTareoEmployee.setPaidHours(savedTareoEmployee.getActualHours());
                        log.info("✓ Calculated paidHours for employee {}: {} hours (based on actual hours - entryMotive isPaid: {}, exitMotive isPaid: {})", 
                                empData.documentNumber(), savedTareoEmployee.getPaidHours(), entryMotiveIsPaid, exitMotiveIsPaid);
                    }
                    tareoEmployeeRepository.save(savedTareoEmployee);
                } else {
                    log.warn("⚠ Cannot calculate paidHours for employee {}: actualHours is null", empData.documentNumber());
                }

                successfulEmployees++;
                log.info("✓ Employee {} processed successfully", empData.documentNumber());

            } catch (IdentifierNotFoundException e) {
                log.error("❌ Employee not found: {}", empData.documentNumber());
                employeeErrors.add(ErrorDetail.of(
                        empData.documentNumber(),
                        "EMPLOYEE_NOT_FOUND",
                        "Employee with document number " + empData.documentNumber() + " does not exist"
                ));
                failedEmployees++;
            } catch (Exception e) {
                log.error("❌ Error processing employee {}: {} - {}", 
                        empData.documentNumber(), e.getClass().getSimpleName(), e.getMessage(), e);
                employeeErrors.add(ErrorDetail.of(
                        empData.documentNumber(),
                        "EMPLOYEE_PROCESSING_ERROR",
                        "Error processing employee: " + e.getMessage()
                ));
                failedEmployees++;
            }
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
        
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("Employee processing completed - Successful: {}, Failed: {}, Total: {}", 
                successfulEmployees, failedEmployees, totalEmployeesToProcess);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        BatchSummary employeeSummary = BatchSummary.of(
                successfulEmployees + failedEmployees,
                successfulEmployees,
                failedEmployees,
                0 // No hay éxito parcial a nivel de empleado individual
        );

        BatchTareoResultData resultData = BatchTareoResultData.of(savedTareo.getPublicId(), employeeSummary);

        // Determinar el resultado del tareo basado en los empleados procesados
        if (totalEmployeesToProcess == 0) {
            log.warn("⚠️ Tareo {} no tiene empleados para procesar", tareoData.temporalId());
            return BatchItemResult.error(
                    tareoData.temporalId(),
                    "NO_EMPLOYEES_TO_PROCESS",
                    "El tareo no tiene empleados para procesar"
            );
        } else if (failedEmployees == 0) {
            log.info("✅ Tareo {} procesado exitosamente - todos los empleados fueron agregados", tareoData.temporalId());
            return BatchItemResult.success(tareoData.temporalId(), resultData);
        } else if (successfulEmployees == 0) {
            // TODOS los empleados fallaron - el tareo debe marcarse como ERROR
            log.error("❌ Tareo {} falló completamente - todos los empleados fallaron ({} fallidos)", 
                    tareoData.temporalId(), failedEmployees);
            log.error("   Errores: {}", employeeErrors);
            return BatchItemResult.error(
                    tareoData.temporalId(),
                    "ALL_EMPLOYEES_FAILED",
                    String.format("Todos los empleados fallaron al procesarse. Errores: %s", 
                            employeeErrors.stream()
                                    .map(e -> e.message())
                                    .collect(Collectors.joining("; ")))
            );
        } else {
            // Algunos empleados exitosos, algunos fallidos - PARTIAL_SUCCESS
            log.warn("⚠️ Tareo {} procesado parcialmente - {} exitosos, {} fallidos", 
                    tareoData.temporalId(), successfulEmployees, failedEmployees);
            log.warn("   Errores de empleados fallidos: {}", employeeErrors);
            return new BatchItemResult<>(
                    tareoData.temporalId(),
                    ResultStatus.PARTIAL_SUCCESS,
                    resultData,
                    employeeErrors
            );
        }
    }

    @Override
    public TareoEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find tareo entity with public ID: {}", publicId);
        return tareoRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Tareo with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.assignment.tareo.not-found", publicId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSyncResponse> getEmployeesForSync(UUID tareoPublicId) {
        log.info("Fetching employees with QR codes for tareo: {}", tareoPublicId);

        TareoEntity tareo = findByPublicId(tareoPublicId);
        LocalDate tareoDate = tareo.getCreatedAt().toLocalDate();

        List<TareoEmployeeEntity> tareoEmployees = tareoEmployeeRepository.findAllByTareoId(tareo.getId());
        log.info("Found {} employees for tareo {}", tareoEmployees.size(), tareoPublicId);

        return tareoEmployees.stream().map(te -> {
            EmployeeEntity emp = te.getEmployee();

            // Obtener QR Rolls asignados a este empleado en la fecha del tareo
            List<QrRollEmployeeEntity> qrRollAssignments = qrRollEmployeeRepository
                    .findByEmployeeCodeAndDate(emp.getCode(), tareoDate)
                    .map(List::of)
                    .orElse(List.of());

            // Mapear QR Rolls con sus QR Codes
            List<EmployeeSyncResponse.QrRollData> qrRolls = qrRollAssignments.stream()
                    .map(qrRollEmp -> {
                        // Obtener códigos QR del roll
                        List<QrCodeEntity> qrCodes = qrCodeRepository.findByRollId(qrRollEmp.getQrRoll().getId());

                        List<EmployeeSyncResponse.QrCodeData> qrCodeData = qrCodes.stream()
                                .map(qc -> new EmployeeSyncResponse.QrCodeData(
                                        qc.getPublicId(),
                                        qc.getIsUsed(),
                                        qc.getIsPrinted()
                                ))
                                .toList();

                        return new EmployeeSyncResponse.QrRollData(
                                qrRollEmp.getPublicId(),
                                qrRollEmp.getQrRoll().getPublicId(),
                                qrRollEmp.getQrRoll().getMaxQrCodesPerDay(),
                                qrCodeData
                        );
                    })
                    .toList();

            return new EmployeeSyncResponse(
                    emp.getCode(),
                    emp.getPersonDocumentNumber(),
                    emp.getPerson() != null ? emp.getPerson().getNames() : null,
                    emp.getPerson() != null ? emp.getPerson().getPaternalLastname() : null,
                    emp.getPerson() != null ? emp.getPerson().getMaternalLastname() : null,
                    emp.getPosition() != null ? emp.getPosition().getName() : null,
                    qrRolls
            );
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<TareoDailyDTO> findAllDailyPaged(UUID laborPublicId, UUID subsidiaryPublicId,
                                                         LocalDate dateFrom, LocalDate dateTo,
                                                         Boolean isCalculated, Pageable pageable) {
        log.info("Fetching daily tareos with filters - laborPublicId: {}, subsidiaryPublicId: {}, dateFrom: {}, dateTo: {}, isCalculated: {}, page: {}, size: {}",
                laborPublicId, subsidiaryPublicId, dateFrom, dateTo, isCalculated, pageable.getPageNumber(), pageable.getPageSize());

        Specification<TareoEntity> spec = TareoSpecification.filterBy(laborPublicId, subsidiaryPublicId, null, dateFrom, dateTo, null);
        Page<TareoEntity> tareoPage = tareoRepository.findAll(spec, pageable);

        Page<TareoDailyDTO> enrichedPage = tareoPage.map(tareo -> {
            long employeeCount = tareoRepository.countTareoEmployeesByTareoId(tareo.getId());
            
            Boolean calculated = payrollDetailRepository.isTareoIdCalculated(tareo.getId());
            if (calculated == null) {
                calculated = false;
            }
            
            if (isCalculated != null && !calculated.equals(isCalculated)) {
                return null; // Filtrar este tareo
            }

            LocalDate tareoDate = tareo.getCreatedAt().toLocalDate();
            String loteName = tareo.getLote() != null ? tareo.getLote().getName() : null;
            UUID lotePublicId = tareo.getLote() != null ? tareo.getLote().getPublicId() : null;
            
            String subsidiaryName = tareo.getSubsidiary() != null ? tareo.getSubsidiary().getName() : null;
            UUID subsidiaryPublicIdValue = tareo.getSubsidiary() != null ? tareo.getSubsidiary().getPublicId() : null;
            
            return new TareoDailyDTO(
                tareo.getPublicId(),
                tareoDate,
                tareo.getLabor() != null ? tareo.getLabor().getName() : null,
                tareo.getLabor() != null ? tareo.getLabor().getPublicId() : null,
                loteName,
                lotePublicId,
                subsidiaryName,
                subsidiaryPublicIdValue,
                tareo.getSupervisor() != null && tareo.getSupervisor().getPerson() != null 
                    ? tareo.getSupervisor().getPerson().getNames() : null,
                tareo.getSupervisor() != null ? tareo.getSupervisor().getPersonDocumentNumber() : null,
                tareo.getScanner() != null && tareo.getScanner().getPerson() != null 
                    ? tareo.getScanner().getPerson().getNames() : null,
                tareo.getScanner() != null ? tareo.getScanner().getPersonDocumentNumber() : null,
                employeeCount,
                calculated,
                tareo.getCreatedAt()
            );
        });

        List<TareoDailyDTO> filteredContent = enrichedPage.getContent().stream()
            .filter(dto -> dto != null)
            .toList();

        Page<TareoDailyDTO> filteredPage = new org.springframework.data.domain.PageImpl<>(
            filteredContent,
            pageable,
            filteredContent.size()
        );

        return new PagedResult<>(filteredPage);
    }
}