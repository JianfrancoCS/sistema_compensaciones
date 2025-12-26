package com.agropay.core.assignment.application.services;

import com.agropay.core.assignment.application.usecase.ILaborUnitUseCase;
import com.agropay.core.assignment.domain.LaborUnitEntity;
import com.agropay.core.assignment.mapper.ILaborUnitMapper;
import com.agropay.core.assignment.model.laborunit.*;
import com.agropay.core.assignment.persistence.ILaborUnitRepository;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.agropay.core.assignment.persistence.LaborUnitSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LaborUnitService implements ILaborUnitUseCase {

    private final ILaborUnitRepository laborUnitRepository;
    private final ILaborUnitMapper laborUnitMapper;

    @Override
    @Transactional
    public CommandLaborUnitResponse create(CreateLaborUnitRequest request) {
        log.info("Attempting to create a new labor unit with name: {}", request.name());

        if (laborUnitRepository.existsByNameIgnoreCase(request.name())) {
            log.warn("Attempted to create a labor unit with a name that already exists: {}", request.name());
            throw new UniqueValidationException("exception.assignment.labor-unit.name-already-exists", request.name());
        }

        if (laborUnitRepository.existsByAbbreviationIgnoreCase(request.abbreviation())) {
            log.warn("Attempted to create a labor unit with an abbreviation that already exists: {}", request.abbreviation());
            throw new UniqueValidationException("exception.assignment.labor-unit.abbreviation-already-exists", request.abbreviation());
        }

        LaborUnitEntity newLaborUnit = laborUnitMapper.toEntity(request);
        LaborUnitEntity savedLaborUnit = laborUnitRepository.save(newLaborUnit);
        log.info("Successfully created labor unit with public ID: {}", savedLaborUnit.getPublicId());
        return laborUnitMapper.toResponse(savedLaborUnit);
    }

    @Override
    @Transactional
    public CommandLaborUnitResponse update(UUID publicId, UpdateLaborUnitRequest request) {
        log.info("Attempting to update labor unit with public ID: {}", publicId);
        LaborUnitEntity existingLaborUnit = findByPublicId(publicId);

        if (laborUnitRepository.existsByNameIgnoreCaseAndPublicIdNot(request.name(), publicId)) {
            log.warn("Attempted to update labor unit {} with a name that already exists: {}", publicId, request.name());
            throw new UniqueValidationException("exception.assignment.labor-unit.name-already-exists", request.name());
        }

        if (laborUnitRepository.existsByAbbreviationIgnoreCaseAndPublicIdNot(request.abbreviation(), publicId)) {
            log.warn("Attempted to update labor unit {} with an abbreviation that already exists: {}", publicId, request.abbreviation());
            throw new UniqueValidationException("exception.assignment.labor-unit.abbreviation-already-exists", request.abbreviation());
        }

        laborUnitMapper.updateEntityFromRequest(request, existingLaborUnit);
        LaborUnitEntity updatedLaborUnit = laborUnitRepository.save(existingLaborUnit);
        log.info("Successfully updated labor unit with public ID: {}", updatedLaborUnit.getPublicId());
        return laborUnitMapper.toResponse(updatedLaborUnit);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete labor unit with public ID: {}", publicId);
        LaborUnitEntity laborUnitToDelete = findByPublicId(publicId);

        long laborCount = laborUnitRepository.countLaborsByLaborUnitId(laborUnitToDelete.getId());
        if (laborCount > 0) {
            log.warn("Attempted to delete labor unit {} which still has {} associated labor(s).", publicId, laborCount);
            throw new ReferentialIntegrityException("exception.assignment.labor-unit.cannot-delete-has-labors", laborCount);
        }

        laborUnitRepository.delete(laborUnitToDelete);
        log.info("Successfully deleted labor unit with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaborUnitSelectOptionDTO> getSelectOptions() {
        log.info("Fetching all labor units for select options.");
        return laborUnitMapper.toSelectOptionDTOs(laborUnitRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<LaborUnitListDTO> findAllPaged(String name, String abbreviation, Pageable pageable) {
        log.info("Fetching paged list of labor units with filters - name: '{}', abbreviation: '{}', page: {}, size: {}",
                name, abbreviation, pageable.getPageNumber(), pageable.getPageSize());

        Specification<LaborUnitEntity> spec = LaborUnitSpecification.filterBy(name, abbreviation);
        Page<LaborUnitEntity> laborUnitPage = laborUnitRepository.findAll(spec, pageable);
        return laborUnitMapper.toPagedDTO(laborUnitPage);
    }

    @Override
    public LaborUnitEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find labor unit entity with public ID: {}", publicId);
        return laborUnitRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Labor unit with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.assignment.labor-unit.not-found", publicId);
                });
    }
}