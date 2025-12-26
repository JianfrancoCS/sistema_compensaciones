package com.agropay.core.attendance.application.service;

import com.agropay.core.attendance.application.usecase.IMarkingReasonUseCase;
import com.agropay.core.attendance.application.usecase.IMarkingUseCase;
import com.agropay.core.attendance.constant.EntryTypeEnum;
import com.agropay.core.attendance.constant.MarkingReasonEnum;
import com.agropay.core.attendance.domain.MarkingDetailEntity;
import com.agropay.core.attendance.domain.MarkingEntity;
import com.agropay.core.attendance.domain.MarkingReasonEntity;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.attendance.mapper.IMarkingMapper;
import com.agropay.core.attendance.model.marking.EmployeeMarkRequest;
import com.agropay.core.attendance.model.marking.ExternalMarkRequest;
import com.agropay.core.attendance.model.marking.MarkingResponse;
import com.agropay.core.attendance.persistence.IMarkingDetailRepository;
import com.agropay.core.attendance.persistence.IMarkingRepository;
import com.agropay.core.organization.api.IEmployeeAPI;
import com.agropay.core.organization.api.IPersonAPI;
import com.agropay.core.organization.api.ISubsidiaryAPI;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.hiring.persistence.IContractRepository;
import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.states.constant.ContractStateEnum;
import com.agropay.core.shared.events.AttendanceMarkedEvent;
import com.agropay.core.attendance.events.AttendanceMarkingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkingServiceImpl implements IMarkingUseCase {

    private final IMarkingRepository markingRepository;
    private final IMarkingDetailRepository markingDetailRepository;
    private final IMarkingReasonUseCase markingReasonUseCase;
    private final IEmployeeAPI employeeUseCase;
    private final IPersonAPI personAPI;
    private final ISubsidiaryAPI subsidiaryUseCase;
    private final IContractRepository contractRepository;
    private final IMarkingMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MarkingResponse markEmployee(EmployeeMarkRequest request) {
        EmployeeEntity employee = validateEmployeeExists(request.personDocumentNumber());
        SubsidiaryEntity subsidiary = subsidiaryUseCase.findByPublicId(request.subsidiaryPublicId());

        validateEmployeeBelongsToSubsidiary(employee, subsidiary);
        
        validateEmployeeHasActiveContract(request.personDocumentNumber(), subsidiary.getPublicId());

        MarkingReasonEntity markingReason = markingReasonUseCase.findByPublicId(request.markingReasonPublicId());
        validateInternalMarkingReason(markingReason);

        EntryTypeEnum entryType = request.isEntry() ? EntryTypeEnum.ENTRY : EntryTypeEnum.EXIT;

        validateExplicitMarkingRules(request.personDocumentNumber(), entryType);

        MarkingDetailEntity markingDetail = createMarking(
            request.personDocumentNumber(),
            subsidiary,
            markingReason,
            entryType,
            true
        );

        publishAttendanceEvent(markingDetail, employee.getPerson());

        return mapper.toMarkingResponse(markingDetail);
    }

    @Override
    @Transactional
    public MarkingResponse markExternal(ExternalMarkRequest request) {
        SubsidiaryEntity subsidiary = subsidiaryUseCase.findByPublicId(request.subsidiaryPublicId());

        MarkingReasonEntity markingReason = markingReasonUseCase.findByPublicId(request.markingReasonPublicId());
        validateExternalMarkingReason(markingReason);

        PersonEntity person = personAPI.findOrCreatePersonByDni(request.personDocumentNumber());

        EntryTypeEnum entryType = request.isEntry() ? EntryTypeEnum.ENTRY : EntryTypeEnum.EXIT;

        validateExplicitMarkingRules(request.personDocumentNumber(), entryType);

        MarkingDetailEntity markingDetail = createMarking(
            request.personDocumentNumber(),
            subsidiary,
            markingReason,
            entryType,
            false
        );

        publishAttendanceEvent(markingDetail, person);

        return mapper.toMarkingResponse(markingDetail);
    }

    /**
     * Valida que el empleado exista.
     * NO validamos el estado del empleado aquí porque la validación real es el contrato.
     * El estado del empleado puede ser CREADO o ACTIVO, ambos son válidos si tienen contrato firmado.
     */
    private EmployeeEntity validateEmployeeExists(String documentNumber) {
        Optional<EmployeeEntity> employeeOpt = employeeUseCase.findByPersonDocumentNumber(documentNumber);

        if (employeeOpt.isEmpty()) {
            throw new IdentifierNotFoundException(documentNumber);
        }

        EmployeeEntity employee = employeeOpt.get();
        
        // Validar que no esté soft-deleted
        if (employee.getDeletedAt() != null) {
            throw new IdentifierNotFoundException(documentNumber);
        }

        return employee;
    }
    
    /**
     * Valida que el empleado tenga un contrato válido (SIGNED) para la subsidiaria especificada.
     * Esta es la validación principal: un contrato firmado es lo que autoriza a trabajar.
     * 
     * IMPORTANTE: 
     * - Puede haber N contratos para un empleado, pero solo UNO debe estar activo (SIGNED)
     * - Los demás contratos deben estar anulados/cancelados
     * - NO validamos el estado del usuario (isActive) porque eso es solo para acceso al sistema
     * 
     * @param documentNumber Número de documento del empleado
     * @param subsidiaryPublicId ID público de la subsidiaria donde se está marcando
     */
    private void validateEmployeeHasActiveContract(String documentNumber, UUID subsidiaryPublicId) {
        // Buscar el contrato ACTIVO (SIGNED) para esta persona y subsidiaria
        // Solo puede haber un contrato activo, los demás deben estar anulados
        String signedStateCode = ContractStateEnum.SIGNED.getCode();
        Optional<ContractEntity> contractOpt = contractRepository
            .findActiveContractByPersonDocumentNumberAndSubsidiary(
                documentNumber, 
                subsidiaryPublicId, 
                signedStateCode
            );
        
        if (contractOpt.isEmpty()) {
            List<ContractEntity> activeContracts = contractRepository
                .findAllActiveContractsByPersonDocumentNumber(documentNumber, signedStateCode);
            
            if (activeContracts.isEmpty()) {
                throw new BusinessValidationException(
                    "exception.attendance.employee.no-contract", 
                    documentNumber);
            } else {
                String subsidiaryName = activeContracts.get(0).getSubsidiary() != null ? 
                    activeContracts.get(0).getSubsidiary().getName() : "N/A";
                throw new BusinessValidationException(
                    "exception.attendance.employee.contract-wrong-subsidiary",
                    documentNumber,
                    subsidiaryName);
            }
        }
        
        ContractEntity contract = contractOpt.get();
        
        String contractStateCode = contract.getState().getCode();
        
        boolean isContractSigned = ContractStateEnum.SIGNED.getCode().equals(contractStateCode);
        
        if (!isContractSigned) {
            String stateDisplayName = getContractStateDisplayName(contractStateCode);
            throw new BusinessValidationException(
                "exception.attendance.employee.contract-not-signed",
                documentNumber,
                stateDisplayName);
        }
        
        boolean isContractCancelled = ContractStateEnum.CANCELLED.getCode().equals(contractStateCode);
        if (isContractCancelled) {
            throw new BusinessValidationException(
                "exception.attendance.employee.contract-cancelled",
                documentNumber);
        }
        
        LocalDate today = LocalDate.now();
        LocalDate contractEndDate = contract.getExtendedEndDate() != null ? 
            contract.getExtendedEndDate() : contract.getEndDate();
        
        if (contractEndDate != null && today.isAfter(contractEndDate)) {
            throw new BusinessValidationException(
                "exception.attendance.employee.contract-expired",
                documentNumber,
                contractEndDate.toString());
        }
        
        if (today.isBefore(contract.getStartDate())) {
            throw new BusinessValidationException(
                "exception.attendance.employee.contract-not-started",
                documentNumber,
                contract.getStartDate().toString());
        }
    }

    private MarkingReasonEntity getWorkMarkingReason() {
        return markingReasonUseCase.findByCode(MarkingReasonEnum.WORK.getCode());
    }

    private void validateInternalMarkingReason(MarkingReasonEntity markingReason) {
        if (markingReason.getIsInternal() == null || !markingReason.getIsInternal()) {
            throw new BusinessValidationException(
                "exception.attendance.marking-reason.invalid-for-internal",
                markingReason.getName()
            );
        }
    }

    private void validateExternalMarkingReason(MarkingReasonEntity markingReason) {
        if (markingReason.getIsInternal() == null || markingReason.getIsInternal()) {
            throw new BusinessValidationException(
                "exception.attendance.marking-reason.invalid-for-external",
                markingReason.getName()
            );
        }
    }

    private void validateEmployeeBelongsToSubsidiary(EmployeeEntity employee, SubsidiaryEntity subsidiary) {
        if (!employee.getSubsidiary().getPublicId().equals(subsidiary.getPublicId())) {
            throw new BusinessValidationException(
                "exception.attendance.employee.wrong-subsidiary",
                employee.getPerson().getDocumentNumber(),
                subsidiary.getName()
            );
        }
    }

    /**
     * Obtiene el nombre para mostrar del estado del contrato desde el enum.
     * Si el estado no está en el enum, retorna el código del estado.
     */
    private String getContractStateDisplayName(String stateCode) {
        for (ContractStateEnum stateEnum : ContractStateEnum.values()) {
            if (stateEnum.getCode().equals(stateCode)) {
                return stateEnum.getDisplayName();
            }
        }
        return stateCode; 
    }
    
    private void validateExplicitMarkingRules(String documentNumber, EntryTypeEnum entryType) {
        LocalDate today = LocalDate.now();
        List<MarkingDetailEntity> todayMarkings = markingDetailRepository.findByPersonAndDate(documentNumber, today);

        if (entryType == EntryTypeEnum.ENTRY) {
            boolean hasEntryToday = todayMarkings.stream()
                    .anyMatch(MarkingDetailEntity::getIsEntry);

            if (hasEntryToday) {
                throw new UniqueValidationException(
                    "exception.attendance.duplicate-entry-marking",
                    documentNumber
                );
            }
        } else {
            boolean hasEntryToday = todayMarkings.stream()
                    .anyMatch(MarkingDetailEntity::getIsEntry);

            if (!hasEntryToday) {
                throw new BusinessValidationException(
                    "exception.attendance.exit-without-entry",
                    documentNumber
                );
            }

            boolean hasExitToday = todayMarkings.stream()
                    .anyMatch(marking -> !marking.getIsEntry());

            if (hasExitToday) {
                throw new UniqueValidationException(
                    "exception.attendance.duplicate-exit-marking",
                    documentNumber
                );
            }
        }
    }

    private EntryTypeEnum determineEntryType(String documentNumber) {
        LocalDate today = LocalDate.now();
        List<MarkingDetailEntity> todayMarkings = markingDetailRepository.findByPersonAndDate(documentNumber, today);

        if (todayMarkings.isEmpty()) {
            return EntryTypeEnum.ENTRY;
        }

        MarkingDetailEntity lastMarking = todayMarkings.getFirst();

        if (lastMarking.getIsEntry()) {
            return EntryTypeEnum.EXIT;
        } else {
            return EntryTypeEnum.ENTRY;
        }
    }

    private void validateMarkingRules(String documentNumber, EntryTypeEnum entryType) {
        LocalDate today = LocalDate.now();

        if (entryType == EntryTypeEnum.ENTRY) {
            Optional<MarkingDetailEntity> existingEntry = markingDetailRepository
                .findByPersonDateAndType(documentNumber, today, true);
            if (existingEntry.isPresent()) {
                throw new UniqueValidationException(
                    "exception.attendance.duplicate-entry-marking",
                    documentNumber
                );
            }
        } else {
            Optional<MarkingDetailEntity> existingEntry = markingDetailRepository
                .findByPersonDateAndType(documentNumber, today, true);
            if (existingEntry.isEmpty()) {
                throw new BusinessValidationException(
                    "exception.attendance.exit-without-entry",
                    documentNumber
                );
            }

            Optional<MarkingDetailEntity> existingExit = markingDetailRepository
                .findByPersonDateAndType(documentNumber, today, false);
            if (existingExit.isPresent()) {
                throw new UniqueValidationException(
                    "exception.attendance.duplicate-exit-marking",
                    documentNumber
                );
            }
        }
    }

    private MarkingDetailEntity createMarking(String documentNumber, SubsidiaryEntity subsidiary,
                                            MarkingReasonEntity markingReason, EntryTypeEnum entryType, boolean isEmployee) {

        MarkingEntity marking = MarkingEntity.builder()
            .publicId(UUID.randomUUID())
            .subsidiary(subsidiary)
            .markingDate(LocalDate.now())
            .build();

        MarkingEntity savedMarking = markingRepository.save(marking);

        MarkingDetailEntity markingDetail = MarkingDetailEntity.builder()
            .publicId(UUID.randomUUID())
            .marking(savedMarking)
            .markingReason(markingReason)
            .personDocumentNumber(documentNumber)
            .isEntry(entryType.isEntry())
            .isEmployee(isEmployee)
            .markedAt(LocalDateTime.now())
            .build();

        return markingDetailRepository.save(markingDetail);
    }

    /**
     * Publish attendance event for asynchronous processing
     * @param markingDetail The saved marking detail entity
     * @param person The person who marked attendance
     */
    private void publishAttendanceEvent(MarkingDetailEntity markingDetail, PersonEntity person) {
        try {
            // Existing event for legacy listeners
            AttendanceMarkedEvent event = AttendanceMarkedEvent.builder()
                .markingDetailPublicId(markingDetail.getPublicId())
                .personDocumentNumber(markingDetail.getPersonDocumentNumber())
                .personFullName(person.getNames() + " " + person.getPaternalLastname()+ " "+person.getMaternalLastname())
                .isEmployee(markingDetail.getIsEmployee())
                .isEntry(markingDetail.getIsEntry())
                .markedAt(markingDetail.getMarkedAt())
                .markingReasonCode(markingDetail.getMarkingReason().getCode())
                .markingReasonName(markingDetail.getMarkingReason().getName())
                .subsidiaryPublicId(markingDetail.getMarking().getSubsidiary().getPublicId())
                .subsidiaryName(markingDetail.getMarking().getSubsidiary().getName())
                .build();

            eventPublisher.publishEvent(event);

            AttendanceMarkingEvent webSocketEvent = new AttendanceMarkingEvent(
                this,
                markingDetail.getMarking().getSubsidiary().getPublicId(),
                markingDetail.getMarking().getMarkingDate(),
                markingDetail.getPersonDocumentNumber(),
                markingDetail.getIsEmployee(),
                markingDetail.getIsEntry()
            );

            eventPublisher.publishEvent(webSocketEvent);

        } catch (Exception e) {
            System.err.println("Error publishing attendance event: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmployeeMarkedEntryOnDate(String documentNumber, LocalDate date) {
        return markingDetailRepository.hasEmployeeMarkedEntryOnDate(documentNumber, date);
    }
}