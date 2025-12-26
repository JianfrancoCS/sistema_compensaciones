package com.agropay.core.payroll.service;

import com.agropay.core.payroll.service.usecase.IPayrollConceptAssignmentService;
import com.agropay.core.payroll.mapper.PayrollConceptAssignmentMapper;
import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.domain.PayrollConceptAssignmentEntity;
import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.model.config.PayrollConceptAssignmentDTO;
import com.agropay.core.payroll.model.config.UpdatePayrollConceptsRequest;
import com.agropay.core.payroll.persistence.IConceptRepository;
import com.agropay.core.payroll.persistence.IPayrollConceptAssignmentRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollConceptAssignmentService implements IPayrollConceptAssignmentService {

    private final IPayrollRepository payrollRepository;
    private final IConceptRepository conceptRepository;
    private final IPayrollConceptAssignmentRepository payrollConceptAssignmentRepository;
    private final PayrollConceptAssignmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PayrollConceptAssignmentDTO> getConceptAssignments(UUID payrollPublicId) {
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new EntityNotFoundException("Payroll not found with publicId: " + payrollPublicId));

        List<ConceptEntity> allConcepts = conceptRepository.findAll();
        Set<UUID> assignedConceptIds = payroll.getPayrollConceptAssignments().stream()
            .map(config -> config.getConcept().getPublicId())
            .collect(Collectors.toSet());

        return allConcepts.stream()
            .map(concept -> mapper.toAssignmentDTO(concept, assignedConceptIds.contains(concept.getPublicId())))
            .collect(Collectors.toList());
    }

    @Override
    public List<PayrollConceptAssignmentDTO> updateConceptAssignments(UUID payrollPublicId, UpdatePayrollConceptsRequest request) {
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new EntityNotFoundException("Payroll not found with publicId: " + payrollPublicId));

        // Step 1: Soft delete all existing configurations for this payroll
        List<PayrollConceptAssignmentEntity> existingAssignments = payrollConceptAssignmentRepository.findByPayrollId(payroll.getId());
        if (!existingAssignments.isEmpty()) {
            payrollConceptAssignmentRepository.deleteAll(existingAssignments); // Assuming soft delete is handled by the framework
        }

        // Step 2: Find the concepts to be assigned from the request
        List<ConceptEntity> conceptsToAssign = conceptRepository.findByPublicIdIn(request.conceptPublicIds());

        // Step 3: Create and save the new assignment entities (with value snapshot)
        List<PayrollConceptAssignmentEntity> newAssignments = conceptsToAssign.stream()
            .map(concept -> PayrollConceptAssignmentEntity.builder()
                .payroll(payroll)
                .concept(concept)
                .value(concept.getValue()) // Snapshot del valor del concepto al momento de la asignación
                .build())
            .toList();

        payrollConceptAssignmentRepository.saveAll(newAssignments);

        // Step 4: Return the new state of assignments
        return getConceptAssignments(payrollPublicId);
    }
}
