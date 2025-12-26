package com.agropay.core.organization.application.services;

import com.agropay.core.organization.application.usecase.IAreaUseCase;
import com.agropay.core.organization.domain.AreaEntity;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.organization.mapper.IAreaMapper;
import com.agropay.core.organization.model.area.*;
import com.agropay.core.organization.persistence.IAreaRepository;
import com.agropay.core.organization.persistence.IPositionRepository;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AreaService implements IAreaUseCase {

    private final IAreaRepository areaRepository;
    private final IPositionRepository positionRepository;
    private final IAreaMapper areaMapper;

    @Override
    @Transactional
    public CommandAreaResponse create(CreateAreaRequest request) {
        log.info("Attempting to create a new area with name: {}", request.name());
        areaRepository.findByNameIgnoreCase(request.name()).ifPresent(a -> {
            log.warn("Attempted to create an area with a name that already exists: {}", request.name());
            throw new UniqueValidationException("exception.organization.area.name-already-exists", request.name());
        });

        AreaEntity newArea = areaMapper.toEntity(request);
        AreaEntity savedArea = areaRepository.save(newArea);
        log.info("Successfully created area with public ID: {}", savedArea.getPublicId());
        return areaMapper.toResponse(savedArea);
    }

    @Override
    @Transactional
    public CommandAreaResponse update(UUID publicId, UpdateAreaRequest request) {
        log.info("Attempting to update area with public ID: {}", publicId);
        AreaEntity existingArea = findByPublicId(publicId);

        areaRepository.findByNameIgnoreCase(request.name()).ifPresent(a -> {
            if (!Objects.equals(a.getPublicId(), publicId)) {
                log.warn("Attempted to update area {} with a name that already exists: {}", publicId, request.name());
                throw new UniqueValidationException("exception.organization.area.name-already-exists", request.name());
            }
        });

        areaMapper.updateEntityFromRequest(request, existingArea);
        AreaEntity updatedArea = areaRepository.save(existingArea);
        log.info("Successfully updated area with public ID: {}", updatedArea.getPublicId());
        return areaMapper.toResponse(updatedArea);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete area with public ID: {}", publicId);
        AreaEntity areaToDelete = findByPublicId(publicId);

        long positionCount = positionRepository.countByAreaId(areaToDelete.getId());
        if (positionCount > 0) {
            log.warn("Attempted to delete area {} which still has {} associated position(s).", publicId, positionCount);
            throw new ReferentialIntegrityException("exception.organization.area.cannot-delete-has-positions");
        }

        areaRepository.delete(areaToDelete);
        log.info("Successfully deleted area with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public AreaDetailsDTO getByPublicId(UUID publicId) {
        log.info("Fetching details for area with public ID: {}", publicId);
        AreaEntity areaWithPositions = areaRepository.findWithPositionsByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.area.not-found", publicId));
        return areaMapper.toDetailsDTO(areaWithPositions);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<AreaListDTO> findAllPaged(String name, Pageable pageable) {
        log.info("Fetching paged list of areas with query: '{}', page: {}, size: {}", name, pageable.getPageNumber(), pageable.getPageSize());
        
        Specification<AreaEntity> spec = (root, q, cb) -> {
            if (StringUtils.hasText(name)) {
                return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
            }
            return cb.conjunction();
        };

        Page<AreaEntity> areaPage = areaRepository.findAll(spec, pageable);
        return areaMapper.toPagedDTO(areaPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaSelectOptionDTO> getSelectOptions() {
        log.info("Fetching all areas for select options.");
        return areaMapper.toSelectOptionDTOs(areaRepository.findAll());
    }

    @Override
    public AreaEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find area entity with public ID: {}", publicId);
        return areaRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Area with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.organization.area.not-found", publicId);
                });
    }


}
