package com.agropay.core.payroll.service;

import com.agropay.core.payroll.domain.ConceptCategoryEntity;
import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.model.concept.*;
import com.agropay.core.payroll.persistence.IConceptCategoryRepository;
import com.agropay.core.payroll.persistence.IConceptRepository;
import com.agropay.core.payroll.persistence.ConceptSpecification;
import com.agropay.core.payroll.persistence.IPayrollConfigurationConceptRepository;
import com.agropay.core.payroll.service.usecase.IConceptService;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.utils.PagedResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConceptService implements IConceptService {

    private final IConceptRepository conceptRepository;
    private final IConceptCategoryRepository conceptCategoryRepository;
    private final IPayrollConfigurationConceptRepository payrollConfigurationConceptRepository;

    @Override
    public List<ConceptSelectOptionDTO> getSelectOptions() {
        return conceptRepository.findAll().stream()
            .map(concept -> new ConceptSelectOptionDTO(
                concept.getPublicId(),
                concept.getName(),
                concept.getValue(),
                concept.getCategory().getName()
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommandConceptResponse create(CreateConceptRequest request) {
        log.info("Creating concept with code: {}", request.code());

        // Verificar que el código no exista
        if (conceptRepository.findAll().stream()
            .anyMatch(c -> c.getCode().equals(request.code()))) {
            throw new BusinessValidationException(
                "exception.concept.code.already-exists",
                request.code()
            );
        }

        // Obtener la categoría
        ConceptCategoryEntity category = conceptCategoryRepository.findByPublicId(request.categoryPublicId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found with publicId: " + request.categoryPublicId()));

        // Crear la entidad
        ConceptEntity concept = ConceptEntity.builder()
            .code(request.code())
            .name(request.name())
            .description(request.description())
            .category(category)
            .value(request.value())
            .calculationPriority(request.calculationPriority())
            .build();

        ConceptEntity savedConcept = conceptRepository.save(concept);
        log.info("Created concept: {} with publicId: {}", savedConcept.getCode(), savedConcept.getPublicId());

        return new CommandConceptResponse(
            savedConcept.getPublicId(),
            savedConcept.getCode(),
            savedConcept.getName(),
            savedConcept.getCreatedAt(),
            savedConcept.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public CommandConceptResponse update(UUID publicId, UpdateConceptRequest request) {
        log.info("Updating concept with publicId: {}", publicId);

        ConceptEntity concept = conceptRepository.findByPublicId(publicId)
            .orElseThrow(() -> new EntityNotFoundException("Concept not found with publicId: " + publicId));

        // Verificar que el código no esté en uso por otro concepto
        if (!concept.getCode().equals(request.code()) &&
            conceptRepository.findAll().stream()
                .anyMatch(c -> c.getCode().equals(request.code()) && !c.getId().equals(concept.getId()))) {
            throw new BusinessValidationException(
                "exception.concept.code.already-exists",
                request.code()
            );
        }

        // Obtener la categoría
        ConceptCategoryEntity category = conceptCategoryRepository.findByPublicId(request.categoryPublicId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found with publicId: " + request.categoryPublicId()));

        // Actualizar la entidad
        concept.setCode(request.code());
        concept.setName(request.name());
        concept.setDescription(request.description());
        concept.setCategory(category);
        concept.setValue(request.value());
        concept.setCalculationPriority(request.calculationPriority());

        ConceptEntity savedConcept = conceptRepository.save(concept);
        log.info("Updated concept: {} with publicId: {}", savedConcept.getCode(), savedConcept.getPublicId());

        return new CommandConceptResponse(
            savedConcept.getPublicId(),
            savedConcept.getCode(),
            savedConcept.getName(),
            savedConcept.getCreatedAt(),
            savedConcept.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Deleting concept with publicId: {}", publicId);

        ConceptEntity concept = conceptRepository.findByPublicId(publicId)
            .orElseThrow(() -> new EntityNotFoundException("Concept not found with publicId: " + publicId));

        // Verificar si está en uso en alguna configuración de planilla
        if (payrollConfigurationConceptRepository.existsByConceptId(concept.getId())) {
            throw new BusinessValidationException(
                "exception.concept.delete.in-use",
                concept.getName()
            );
        }

        conceptRepository.softDelete(concept.getId());
        log.info("Soft-deleted concept: {} with publicId: {}", concept.getCode(), publicId);
    }

    @Override
    public ConceptDetailsDTO getByPublicId(UUID publicId) {
        ConceptEntity concept = conceptRepository.findByPublicId(publicId)
            .orElseThrow(() -> new EntityNotFoundException("Concept not found with publicId: " + publicId));

        return new ConceptDetailsDTO(
            concept.getPublicId(),
            concept.getCode(),
            concept.getName(),
            concept.getDescription(),
            concept.getCategory().getPublicId(),
            concept.getCategory().getName(),
            concept.getValue(),
            concept.getCalculationPriority()
        );
    }

    @Override
    public PagedResult<ConceptListDTO> findAllPaged(String name, UUID categoryPublicId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ConceptEntity> spec = ConceptSpecification.filterBy(name, categoryPublicId, sort);
        Page<ConceptEntity> conceptPage = conceptRepository.findAll(spec, pageable);

        Page<ConceptListDTO> dtoPage = conceptPage.map(concept -> new ConceptListDTO(
            concept.getPublicId(),
            concept.getCode(),
            concept.getName(),
            concept.getDescription(),
            concept.getCategory().getName(),
            concept.getValue(),
            concept.getCalculationPriority(),
            concept.getCreatedAt(),
            concept.getUpdatedAt()
        ));

        return new PagedResult<>(dtoPage);
    }

    @Override
    public List<com.agropay.core.payroll.model.concept.ConceptCategoryOptionDTO> getCategories() {
        return conceptCategoryRepository.findAll().stream()
            .map(category -> new com.agropay.core.payroll.model.concept.ConceptCategoryOptionDTO(
                category.getPublicId(),
                category.getCode(),
                category.getName()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<ConceptSelectOptionDTO> getSelectOptionsByCategoryCode(String categoryCode) {
        log.info("Fetching concepts for category code: {}", categoryCode);
        
        ConceptCategoryEntity category = conceptCategoryRepository.findByCode(categoryCode)
            .orElse(null);
        
        if (category == null) {
            log.warn("Category with code '{}' not found", categoryCode);
            return List.of();
        }
        
        List<ConceptSelectOptionDTO> concepts = conceptRepository.findAll().stream()
            .filter(concept -> {
                if (concept.getCategory() == null) {
                    return false;
                }
                return concept.getCategory().getId().equals(category.getId());
            })
            .map(concept -> new ConceptSelectOptionDTO(
                concept.getPublicId(),
                concept.getName(),
                concept.getValue(),
                concept.getCategory().getName()
            ))
            .collect(Collectors.toList());
        
        log.info("Found {} concepts for category code: {} (category: {})", 
            concepts.size(), categoryCode, category.getName());
        return concepts;
    }
}
