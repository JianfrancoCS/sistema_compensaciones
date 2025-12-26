package com.agropay.core.assignment.application.services;

import com.agropay.core.assignment.application.usecase.IQrRollUseCase;
import com.agropay.core.assignment.domain.QrCodeEntity;
import com.agropay.core.assignment.domain.QrRollEmployeeEntity;
import com.agropay.core.assignment.domain.QrRollEntity;
import com.agropay.core.assignment.mapper.IQrRollMapper;
import com.agropay.core.assignment.model.qrroll.*;
import com.agropay.core.assignment.persistence.IQrCodeRepository;
import com.agropay.core.assignment.persistence.IQrRollEmployeeRepository;
import com.agropay.core.assignment.persistence.IQrRollRepository;
import com.agropay.core.assignment.persistence.QrCodeSpecification;
import com.agropay.core.organization.application.usecase.IEmployeeUseCase;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.shared.batch.BatchItemResult;
import com.agropay.core.shared.batch.BatchResponse;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrRollService implements IQrRollUseCase {

    private final IQrRollRepository qrRollRepository;
    private final IQrRollEmployeeRepository qrRollEmployeeRepository;
    private final IQrCodeRepository qrCodeRepository;
    private final IQrRollMapper qrRollMapper;
    private final IEmployeeUseCase employeeUseCase;

    @Override
    @Transactional
    public CommandQrRollResponse create(CreateQrRollRequest request) {
        log.info("Attempting to create a new QR roll");

        QrRollEntity qrRoll = new QrRollEntity();
        qrRoll.setMaxQrCodesPerDay(request.maxQrCodesPerDay());
        QrRollEntity savedRoll = qrRollRepository.save(qrRoll);

        log.info("Successfully created QR roll with public ID: {}", savedRoll.getPublicId());
        return qrRollMapper.toResponse(savedRoll);
    }

    @Override
    @Transactional
    public CommandQrRollResponse update(UUID publicId, UpdateQrRollRequest request) {
        log.info("Attempting to update QR roll with public ID: {}", publicId);
        QrRollEntity existingRoll = findByPublicId(publicId);

        qrRollMapper.updateEntityFromRequest(request, existingRoll);

        QrRollEntity updatedRoll = qrRollRepository.save(existingRoll);
        log.info("Successfully updated QR roll with public ID: {}", updatedRoll.getPublicId());
        return qrRollMapper.toResponse(updatedRoll);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to soft delete QR roll with public ID: {}", publicId);
        QrRollEntity roll = findByPublicId(publicId);

        Long qrCodeCount = qrCodeRepository.countByRollId(roll.getId());
        if (qrCodeCount > 0) {
            log.warn("Cannot delete roll {} because it has {} associated QR codes.", publicId, qrCodeCount);
            throw new BusinessValidationException("exception.assignment.qr-roll.cannot-delete-with-codes", qrCodeCount);
        }

        qrRollRepository.softDelete(roll.getId(), "system");
        log.info("Successfully soft deleted QR roll with public ID: {}", publicId);
    }

    @Override
    @Transactional
    public void generateQrCodes(UUID rollPublicId, int quantity) {
        log.info("Attempting to generate {} QR codes for roll {}", quantity, rollPublicId);

        QrRollEntity roll = findByPublicId(rollPublicId);

        if (roll.getMaxQrCodesPerDay() != null) {
            Long currentQrCodes = qrCodeRepository.countByRollId(roll.getId());
            long totalAfterGeneration = currentQrCodes + quantity;

            if (totalAfterGeneration > roll.getMaxQrCodesPerDay()) {
                log.warn("Cannot generate {} QR codes for roll {}. Would exceed max limit of {} (current: {}, requested: {})",
                        quantity, rollPublicId, roll.getMaxQrCodesPerDay(), currentQrCodes, quantity);
                throw new BusinessValidationException(
                        "exception.assignment.qr-roll.exceeds-max-qr-codes-per-day",
                        roll.getMaxQrCodesPerDay(), currentQrCodes, quantity);
            }
        }

        List<QrCodeEntity> qrCodes = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            QrCodeEntity qrCodeEntity = new QrCodeEntity();
            qrCodeEntity.setQrRoll(roll);
            qrCodes.add(qrCodeEntity);
        }

        qrCodeRepository.saveAll(qrCodes);
        log.info("Successfully generated {} QR codes for roll {}", quantity, rollPublicId);
    }

    @Override
    @Transactional
    public void printQrCodes(UUID rollPublicId) {
        log.info("Attempting to mark today's QR codes as printed for roll {}", rollPublicId);
        QrRollEntity roll = findByPublicId(rollPublicId);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        List<QrCodeEntity> unprintedCodes = qrCodeRepository.findUnprintedByRollIdAndDate(roll.getId(), startOfDay, endOfDay);

        if (unprintedCodes.isEmpty()) {
            log.warn("No unprinted QR codes found for roll {} for today.", rollPublicId);
            throw new BusinessValidationException("exception.assignment.qr-roll.no-unprinted-codes-for-today");
        }

        unprintedCodes.forEach(qrCode -> qrCode.setIsPrinted(true));
        qrCodeRepository.saveAll(unprintedCodes);

        log.info("Successfully marked {} QR codes as printed for roll {}", unprintedCodes.size(), rollPublicId);
    }

    @Override
    @Transactional
    public void assignToEmployee(UUID rollPublicId, String employeeDocumentNumber) {
        LocalDate assignedDate = LocalDate.now();
        log.info("Attempting to assign roll {} to employee with document number {} for date {}", rollPublicId, employeeDocumentNumber, assignedDate);

        QrRollEntity roll = findByPublicId(rollPublicId);
        EmployeeEntity employee = employeeUseCase.findByDocumentNumber(employeeDocumentNumber);

        if (qrRollEmployeeRepository.existsByEmployeeCodeAndDate(employee.getCode(), assignedDate)) {
            log.warn("Employee with document number {} already has a roll assigned for date {}", employeeDocumentNumber, assignedDate);
            throw new BusinessValidationException("exception.assignment.qr.employee-already-has-roll-for-date", employeeDocumentNumber);
        }

        if (qrRollEmployeeRepository.existsByRollIdAndEmployeeCodeAndDate(roll.getId(), employee.getCode(), assignedDate)) {
            log.warn("Roll {} is already assigned to employee with document number {} for date {}", rollPublicId, employeeDocumentNumber, assignedDate);
            throw new BusinessValidationException("exception.assignment.qr.roll-already-assigned-to-employee-for-date", rollPublicId);
        }

        QrRollEmployeeEntity assignment = new QrRollEmployeeEntity();
        assignment.setQrRoll(roll);
        assignment.setEmployee(employee);
        assignment.setAssignedDate(assignedDate);

        qrRollEmployeeRepository.save(assignment);
        log.info("Successfully assigned roll {} to employee with document number {} for date {}", rollPublicId, employeeDocumentNumber, assignedDate);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<QrRollListDTO> findAllPaged(QrRollPageableRequest request) {
        Pageable pageable = request.toPageable();
        log.info("Fetching paged list of QR rolls - page: {}, size: {}, hasUnprinted: {}",
                pageable.getPageNumber(), pageable.getPageSize(), request.getHasUnprintedCodes());

        Page<QrRollEntity> rollPage;
        if (Boolean.TRUE.equals(request.getHasUnprintedCodes())) {
            rollPage = qrRollRepository.findRollsWithUnprintedCodes(pageable);
        } else {
            rollPage = qrRollRepository.findAll(pageable);
        }

        Page<QrRollListDTO> dtoPage = rollPage.map(roll -> {
            Long totalCodes = qrCodeRepository.countByRollId(roll.getId());
            Long unprintedCodes = qrCodeRepository.countUnprintedByRollId(roll.getId());
            return qrRollMapper.toListDTO(roll, totalCodes, unprintedCodes);
        });

        return new PagedResult<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<PrintStatsDTO> findPrintStats(PrintStatsPageableRequest request) {
        Pageable pageable = request.toPageable();
        LocalDateTime startOfDay = request.getDate().atStartOfDay();
        LocalDateTime endOfDay = request.getDate().plusDays(1).atStartOfDay();

        log.info("Fetching print stats for date: {}, rollPublicId: {}, page: {}, size: {}",
                request.getDate(), request.getRollPublicId(), pageable.getPageNumber(), pageable.getPageSize());

        Page<QrRollEntity> rollPage = qrRollRepository.findRollsWithPrintsByDate(
                startOfDay, endOfDay, request.getRollPublicId(), pageable);

        Page<PrintStatsDTO> dtoPage = rollPage.map(roll -> {
            QrCodeStatsDTO stats = qrCodeRepository.getPrintStatsByRollIdAndDate(roll.getId(), startOfDay, endOfDay);
            long notUsedCount = stats.printedCount() - stats.usedCount();
            return new PrintStatsDTO(roll.getPublicId(), stats.printedCount(), stats.usedCount(), notUsedCount);
        });

        return new PagedResult<>(dtoPage);
    }

    @Override
    public QrRollEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find QR roll entity with public ID: {}", publicId);
        return qrRollRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("QR roll with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.assignment.qr-roll.not-found", publicId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public AvailableQrRollsStatsResponse findAvailableRollsStats(LocalDate date) {
        log.info("Fetching QR rolls statistics for date: {}", date);

        List<QrRollEntity> unassignedRolls = qrRollRepository.findUnassignedRollsForDate(date);

        int totalAvailable = (int) unassignedRolls.stream()
                .filter(roll -> {
                    Long totalCodes = qrCodeRepository.countByRollId(roll.getId());
                    return roll.getMaxQrCodesPerDay() != null && totalCodes >= roll.getMaxQrCodesPerDay();
                })
                .count();

        int totalInUse = (int) qrRollRepository.countAssignedRollsForDate(date);

        log.info("QR rolls stats for date {}: {} available, {} in use", date, totalAvailable, totalInUse);
        return new AvailableQrRollsStatsResponse(totalAvailable, totalInUse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QrCodeDTO> findQrCodesByRoll(UUID rollPublicId, Boolean isUsed, Boolean isPrinted, LocalDate createdDate) {
        log.info("Fetching QR codes for roll: {} with filters - isUsed: {}, isPrinted: {}, createdDate: {}",
                rollPublicId, isUsed, isPrinted, createdDate);

        QrRollEntity roll = findByPublicId(rollPublicId);

        Specification<QrCodeEntity> spec = QrCodeSpecification.filterByRoll(roll.getId(), isUsed, isPrinted, createdDate);
        List<QrCodeEntity> qrCodes = qrCodeRepository.findAll(spec);

        return qrCodes.stream()
                .map(qc -> new QrCodeDTO(qc.getPublicId(), qc.getIsUsed(), qc.getIsPrinted(), qc.getCreatedAt()))
                .toList();
    }

    @Override
    @Transactional
    public void batchGenerateQrCodes(Integer rollsNeeded, Integer codesPerRoll) {
        log.info("Attempting to batch generate QR codes: {} rolls needed, {} codes per roll", rollsNeeded, codesPerRoll);

        List<QrRollEntity> availableRolls = qrRollRepository.findAvailableRolls();

        List<QrRollEntity> rollsWithEnoughCapacity = availableRolls.stream()
                .filter(roll -> {
                    Long currentQrCodes = qrCodeRepository.countByRollId(roll.getId());
                    int remainingCapacity = roll.getMaxQrCodesPerDay() - currentQrCodes.intValue();
                    return remainingCapacity >= codesPerRoll;
                })
                .toList();

        if (rollsWithEnoughCapacity.size() < rollsNeeded) {
            log.warn("Insufficient available rolls with enough capacity. Requested: {}, Available: {}, Capacity per roll needed: {}",
                    rollsNeeded, rollsWithEnoughCapacity.size(), codesPerRoll);
            throw new BusinessValidationException(
                    "exception.assignment.qr-roll.insufficient-available-rolls-with-capacity",
                    rollsNeeded, rollsWithEnoughCapacity.size(), codesPerRoll);
        }

        List<QrRollEntity> rollsToProcess = rollsWithEnoughCapacity.stream()
                .limit(rollsNeeded)
                .toList();

        List<QrCodeEntity> allQrCodes = new ArrayList<>();
        for (QrRollEntity roll : rollsToProcess) {
            for (int i = 0; i < codesPerRoll; i++) {
                QrCodeEntity qrCodeEntity = new QrCodeEntity();
                qrCodeEntity.setQrRoll(roll);
                allQrCodes.add(qrCodeEntity);
            }
        }

        qrCodeRepository.saveAll(allQrCodes);
        log.info("Successfully batch generated {} QR codes across {} rolls", allQrCodes.size(), rollsToProcess.size());
    }

    @Override
    @Transactional
    public BatchResponse<Void> batchAssign(BatchAssignQrRollsRequest request) {
        log.info("Attempting batch assignment with {} QR roll assignments", request.assignments().size());

        List<BatchItemResult<Void>> results = new ArrayList<>();

        for (BatchQrRollAssignmentData assignmentData : request.assignments()) {
            try {
                BatchItemResult<Void> result = processSingleAssignment(assignmentData);
                results.add(result);
            } catch (Exception e) {
                log.error("Unexpected error processing assignment for employee: {}",
                        assignmentData.employeeDocumentNumber(), e);
                results.add(BatchItemResult.error(
                        assignmentData.employeeDocumentNumber(),
                        "UNEXPECTED_ERROR",
                        "Unexpected error: " + e.getMessage()
                ));
            }
        }

        log.info("Batch assignment completed. Total: {}, Successful: {}, Failed: {}",
                results.size(),
                results.stream().filter(BatchItemResult::isSuccess).count(),
                results.stream().filter(BatchItemResult::isError).count());

        return BatchResponse.of(results);
    }

    private BatchItemResult<Void> processSingleAssignment(BatchQrRollAssignmentData assignmentData) {
        log.info("Processing assignment via QR code {} to employee {}",
                assignmentData.qrCodePublicId(), assignmentData.employeeDocumentNumber());

        // 1. Buscar el QR code por su publicId
        QrCodeEntity qrCode = qrCodeRepository.findByPublicId(assignmentData.qrCodePublicId())
                .orElseGet(() -> {
                    log.warn("QR code with public ID {} not found", assignmentData.qrCodePublicId());
                    return null;
                });

        if (qrCode == null) {
            return BatchItemResult.error(
                    assignmentData.employeeDocumentNumber(),
                    "QR_CODE_NOT_FOUND",
                    "QR Code does not exist"
            );
        }

        // 2. Obtener el roll asociado a este QR code
        QrRollEntity roll = qrCode.getQrRoll();
        log.info("QR code {} belongs to roll {}", assignmentData.qrCodePublicId(), roll.getPublicId());

        // 3. Validar que el empleado exista
        EmployeeEntity employee;
        try {
            employee = employeeUseCase.findByDocumentNumber(assignmentData.employeeDocumentNumber());
        } catch (IdentifierNotFoundException e) {
            return BatchItemResult.error(
                    assignmentData.employeeDocumentNumber(),
                    "EMPLOYEE_NOT_FOUND",
                    "Employee with document number " + assignmentData.employeeDocumentNumber() + " does not exist"
            );
        }

        LocalDate today = LocalDate.now();

        // 4. Verificar si el roll ya está asignado hoy
        var existingAssignment = qrRollEmployeeRepository.findByRollAndDate(roll.getId(), today);

        if (existingAssignment.isPresent()) {
            QrRollEmployeeEntity existing = existingAssignment.get();

            // Si está asignado al mismo empleado → OK (idempotencia)
            if (existing.getEmployee().getCode().equals(employee.getCode())) {
                log.info("Roll {} already assigned to employee {} today - idempotent operation",
                        roll.getPublicId(), assignmentData.employeeDocumentNumber());
                return BatchItemResult.success(assignmentData.employeeDocumentNumber(), null);
            }

            // Si está asignado a otro empleado → ERROR
            log.warn("Roll {} already assigned to another employee today", roll.getPublicId());
            return BatchItemResult.error(
                    assignmentData.employeeDocumentNumber(),
                    "ROLL_ASSIGNED_TO_ANOTHER",
                    "This QR roll is already assigned to another employee for today"
            );
        }

        // 5. Crear la asignación
        QrRollEmployeeEntity assignment = new QrRollEmployeeEntity();
        assignment.setQrRoll(roll);
        assignment.setEmployee(employee);
        assignment.setAssignedDate(today);

        qrRollEmployeeRepository.save(assignment);

        log.info("Successfully assigned roll {} to employee {} via QR code {}",
                roll.getPublicId(), assignmentData.employeeDocumentNumber(), assignmentData.qrCodePublicId());

        return BatchItemResult.success(assignmentData.employeeDocumentNumber(), null);
    }

}