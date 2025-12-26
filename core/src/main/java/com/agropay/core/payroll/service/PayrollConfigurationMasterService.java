package com.agropay.core.payroll.service;

import com.agropay.core.payroll.mapper.PayrollConfigurationMasterMapper;
import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.domain.PayrollConfigurationConceptEntity;
import com.agropay.core.payroll.domain.PayrollConfigurationEntity;
import com.agropay.core.payroll.model.masterconfig.CommandPayrollConfigurationResponse;
import com.agropay.core.payroll.model.masterconfig.CreatePayrollConfigurationRequest;
import com.agropay.core.payroll.model.masterconfig.PayrollConfigurationConceptAssignmentDTO;
import com.agropay.core.payroll.model.masterconfig.UpdateConceptAssignmentsRequest;
import com.agropay.core.payroll.persistence.IConceptRepository;
import com.agropay.core.payroll.persistence.IPayrollConfigurationConceptRepository;
import com.agropay.core.payroll.persistence.IPayrollMasterConfigurationRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import com.agropay.core.payroll.service.usecase.IPayrollConfigurationMasterService;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PayrollConfigurationMasterService implements IPayrollConfigurationMasterService {

    private final IPayrollMasterConfigurationRepository payrollConfigurationRepository;
    private final IPayrollConfigurationConceptRepository payrollConfigurationConceptRepository;
    private final IConceptRepository conceptRepository;
    private final IPayrollRepository payrollRepository;
    private final PayrollConfigurationMasterMapper mapper;

    @Override
    public CommandPayrollConfigurationResponse createConfiguration(CreatePayrollConfigurationRequest request) {
        payrollConfigurationRepository.findTop1ByDeletedAtIsNullOrderByIdDesc().ifPresent(activeConfig -> {
            activeConfig.setDeletedAt(LocalDateTime.now());
            activeConfig.setDeletedBy("SYSTEM");
            payrollConfigurationRepository.save(activeConfig);
            log.info("Soft-deleted previous active payroll configuration: {}", activeConfig.getCode());
        });

        String newCode = "CONF_PLANILLA_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        PayrollConfigurationEntity newConfig = PayrollConfigurationEntity.builder()
            .publicId(UUID.randomUUID())
            .code(newCode)
            .build();

        payrollConfigurationRepository.save(newConfig);
        log.info("Created new payroll configuration: {}", newConfig.getCode());

        if (request.conceptsPublicIds() != null && !request.conceptsPublicIds().isEmpty()) {
            updateConceptsForConfiguration(newConfig, request.conceptsPublicIds());
        }

        return mapper.toCommandResponse(newConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandPayrollConfigurationResponse getActiveConfiguration() {
        return payrollConfigurationRepository.findTop1ByDeletedAtIsNullOrderByIdDesc()
            .map(mapper::toCommandResponse)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollConfigurationConceptAssignmentDTO> getConceptAssignmentsForActiveConfiguration() {
        List<ConceptEntity> allConcepts = conceptRepository.findAll();
        
        return payrollConfigurationRepository.findTop1ByDeletedAtIsNullOrderByIdDesc()
            .map(config -> {
                Set<UUID> assignedConceptPublicIds = config.getConcepts().stream()
                    .map(PayrollConfigurationConceptEntity::getConcept)
                    .map(ConceptEntity::getPublicId)
                    .collect(Collectors.toSet());

                return allConcepts.stream()
                    .map(concept -> new PayrollConfigurationConceptAssignmentDTO(
                        concept.getPublicId(),
                        concept.getName(),
                        assignedConceptPublicIds.contains(concept.getPublicId())
                    ))
                    .collect(Collectors.toList());
            })
            .orElseGet(() -> {
                // If no active configuration, return all concepts as unassigned
                return allConcepts.stream()
                    .map(concept -> new PayrollConfigurationConceptAssignmentDTO(
                        concept.getPublicId(),
                        concept.getName(),
                        false // Not assigned
                    ))
                    .collect(Collectors.toList());
            });
    }

    @Override
    public List<PayrollConfigurationConceptAssignmentDTO> updateConceptAssignmentsForActiveConfiguration(UpdateConceptAssignmentsRequest request) {
        PayrollConfigurationEntity config = payrollConfigurationRepository.findTop1ByDeletedAtIsNullOrderByIdDesc()
            .orElseThrow(() -> new BusinessValidationException("exception.payroll.configuration.not-found"));

        List<UUID> conceptPublicIds = request.conceptPublicIds();

        if (payrollRepository.existsByPayrollConfigurationId(config.getId())) {
            config.setDeletedAt(LocalDateTime.now());
            config.setDeletedBy("SYSTEM");
            payrollConfigurationRepository.save(config);
            log.info("Soft-deleted referenced payroll configuration: {}", config.getCode());

            String newCode = "CONF_PLANILLA_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            PayrollConfigurationEntity newConfig = PayrollConfigurationEntity.builder()
                .publicId(UUID.randomUUID())
                .code(newCode)
                .build();
            payrollConfigurationRepository.save(newConfig);
            log.info("Created new payroll configuration due to update of referenced config: {}", newConfig.getCode());

            updateConceptsForConfiguration(newConfig, conceptPublicIds);
            return getConceptAssignmentsForActiveConfiguration();
        } else {
            updateConceptsForConfiguration(config, conceptPublicIds);
            return getConceptAssignmentsForActiveConfiguration();
        }
    }

    @Override
    public void deleteActiveConfiguration() {
        PayrollConfigurationEntity configToDelete = payrollConfigurationRepository.findTop1ByDeletedAtIsNullOrderByIdDesc()
            .orElseThrow(() -> new BusinessValidationException("exception.payroll.configuration.not-found"));

        if (payrollRepository.existsByPayrollConfigurationId(configToDelete.getId())) {
            throw new BusinessValidationException("exception.payroll.configuration.delete.referenced");
        }

        payrollConfigurationRepository.softDelete(configToDelete.getId(), "SYSTEM");
        log.info("Soft-deleted active payroll configuration: {}", configToDelete.getCode());
    }

    private void updateConceptsForConfiguration(PayrollConfigurationEntity config, List<UUID> conceptPublicIds) {
        // Soft delete todos los conceptos existentes
        if (config.getConcepts() != null && !config.getConcepts().isEmpty()) {
            payrollConfigurationConceptRepository.softDeleteAll(config.getConcepts(), "SYSTEM");
        }

        // Limpiar la colección en lugar de reemplazarla (evita error de orphanRemoval)
        if (config.getConcepts() != null) {
            config.getConcepts().clear();
        }

        if (conceptPublicIds == null || conceptPublicIds.isEmpty()) {
            return;
        }

        // Crear nuevos assignments
        List<PayrollConfigurationConceptEntity> newAssignments = conceptPublicIds.stream()
            .map(conceptPublicId -> {
                ConceptEntity concept = conceptRepository.findByPublicId(conceptPublicId)
                    .orElseThrow(() -> new BusinessValidationException("exception.payroll.concept.not-found", (Object[]) new String[]{conceptPublicId.toString()}));
                return PayrollConfigurationConceptEntity.builder()
                    .publicId(UUID.randomUUID())
                    .payrollConfiguration(config)
                    .concept(concept)
                    .value(concept.getValue())
                    .build();
            })
            .collect(Collectors.toList());

        // Guardar los nuevos assignments
        payrollConfigurationConceptRepository.saveAll(newAssignments);

        // Agregar a la colección existente en lugar de reemplazarla
        if (config.getConcepts() == null) {
            config.setConcepts(new HashSet<>());
        }
        config.getConcepts().addAll(newAssignments);
    }
}
