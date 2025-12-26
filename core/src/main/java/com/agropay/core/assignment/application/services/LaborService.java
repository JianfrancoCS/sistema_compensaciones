package com.agropay.core.assignment.application.services;

import com.agropay.core.assignment.application.usecase.ILaborUnitUseCase;
import com.agropay.core.assignment.application.usecase.ILaborUseCase;
import com.agropay.core.assignment.domain.LaborEntity;
import com.agropay.core.assignment.domain.LaborUnitEntity;
import com.agropay.core.assignment.mapper.ILaborMapper;
import com.agropay.core.assignment.model.labor.*;
import com.agropay.core.assignment.persistence.ILaborRepository;
import com.agropay.core.assignment.persistence.LaborSpecification;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class LaborService implements ILaborUseCase {

    private final ILaborRepository laborRepository;
    private final ILaborMapper laborMapper;
    private final ILaborUnitUseCase laborUnitUseCase;

    @Override
    @Transactional
    public CommandLaborResponse create(CreateLaborRequest request) {
        log.info("Attempting to create a new labor with name: {}", request.name());

        LaborUnitEntity laborUnit = laborUnitUseCase.findByPublicId(request.laborUnitPublicId());

        if (laborRepository.existsByName(request.name())) {
            log.warn("Attempted to create a labor with a name that already exists: {}", request.name());
            throw new UniqueValidationException("exception.assignment.labor.name-already-exists", request.name());
        }

        LaborEntity newLabor = laborMapper.toEntity(request);
        newLabor.setLaborUnit(laborUnit);

        LaborEntity savedLabor = laborRepository.save(newLabor);
        log.info("Successfully created labor with public ID: {}", savedLabor.getPublicId());
        return laborMapper.toResponse(savedLabor);
    }

    @Override
    @Transactional
    public CommandLaborResponse update(UUID publicId, UpdateLaborRequest request) {
        log.info("Attempting to update labor with public ID: {}", publicId);
        LaborEntity existingLabor = findByPublicId(publicId);

        LaborUnitEntity laborUnit = laborUnitUseCase.findByPublicId(request.laborUnitPublicId());

        if (laborRepository.existsByNameAndPublicIdNot(request.name(), publicId)) {
            log.warn("Attempted to update labor {} with a name that already exists: {}", publicId, request.name());
            throw new UniqueValidationException("exception.assignment.labor.name-already-exists", request.name());
        }

        laborMapper.updateEntityFromRequest(request, existingLabor);
        existingLabor.setLaborUnit(laborUnit);

        LaborEntity updatedLabor = laborRepository.save(existingLabor);
        log.info("Successfully updated labor with public ID: {}", updatedLabor.getPublicId());
        return laborMapper.toResponse(updatedLabor);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete labor with public ID: {}", publicId);
        LaborEntity laborToDelete = findByPublicId(publicId);

        long tareoCount = laborRepository.countTareosByLaborId(laborToDelete.getId());
        if (tareoCount > 0) {
            log.warn("Attempted to delete labor {} which still has {} associated tareo(s).", publicId, tareoCount);
            throw new ReferentialIntegrityException("exception.assignment.labor.cannot-delete-has-tareos", tareoCount);
        }

        laborRepository.delete(laborToDelete);
        log.info("Successfully deleted labor with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaborSelectOptionDTO> getSelectOptions() {
        log.info("Fetching all labors for select options.");
        return laborMapper.toSelectOptionDTOs(laborRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<LaborListDTO> findAllPaged(String name, Boolean isPiecework, UUID laborUnitPublicId, Pageable pageable) {
        log.info("Fetching paged list of labors with filters - name: '{}', isPiecework: {}, laborUnitPublicId: {}, page: {}, size: {}",
                name, isPiecework, laborUnitPublicId, pageable.getPageNumber(), pageable.getPageSize());

        Specification<LaborEntity> spec = LaborSpecification.filterBy(name, isPiecework, laborUnitPublicId);
        Page<LaborEntity> laborPage = laborRepository.findAll(spec, pageable);
        return laborMapper.toPagedDTO(laborPage);
    }

    @Override
    public LaborEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find labor entity with public ID: {}", publicId);
        return laborRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Labor with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.assignment.labor.not-found", publicId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaborSyncResponse> findAllForSync() {
        log.info("Fetching all labors for mobile sync.");
        return laborRepository.findAll().stream()
                .map(labor -> new LaborSyncResponse(
                        labor.getPublicId(),
                        labor.getName(),
                        labor.getDescription(),
                        labor.getIsPiecework(),
                        labor.getLaborUnit() != null ? labor.getLaborUnit().getName() : null,
                        labor.getMinTaskRequirement(),
                        labor.getBasePrice()
                ))
                .toList();
    }
}