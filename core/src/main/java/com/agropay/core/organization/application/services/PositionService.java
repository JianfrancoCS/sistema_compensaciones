package com.agropay.core.organization.application.services;

import com.agropay.core.shared.utils.ApiResult;
// TODO: Eliminar AuthClient - ya no hay servicio auth separado
import com.agropay.core.organization.application.usecase.IAreaUseCase;
import com.agropay.core.organization.application.usecase.IPositionUseCase;
import com.agropay.core.organization.domain.AreaEntity;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.organization.exception.PositionSelfReferenceException;
import com.agropay.core.organization.mapper.IPositionMapper;
import com.agropay.core.organization.model.position.*;
import com.agropay.core.organization.persistence.IEmployeeRepository;
import com.agropay.core.organization.persistence.IPositionRepository;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.shared.utils.PagedResult;
import jakarta.persistence.criteria.Predicate;
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
public class PositionService implements IPositionUseCase {

    private final IPositionRepository positionRepository;
    private final IEmployeeRepository employeeRepository;
    private final IAreaUseCase areaUseCase;
    private final IPositionMapper positionMapper;
    // TODO: Eliminar AuthClient - ya no hay servicio auth separado
    // private final AuthClient authClient;

    @Override
    @Transactional
    public CommandPositionResponse create(CreatePositionRequest request) {
        log.info("Attempting to create a new position with name: {} in area: {}", request.name(), request.areaPublicId());

        AreaEntity area = areaUseCase.findByPublicId(request.areaPublicId());
        positionRepository.findByNameAndAreaId(request.name(), area.getId()).ifPresent(p -> {
            log.warn("Attempted to create a position with a name that already exists in the area: \'{}' in \'{}'", request.name(), area.getName());
            throw new UniqueValidationException("exception.organization.position.name-already-exists-in-area", request.name(), area.getName());
        });
        PositionEntity newPosition = positionMapper.toEntity(request);
        newPosition.setArea(area);

        if (request.requiredManagerPositionPublicId() != null) {
            if (newPosition.getPublicId() != null && newPosition.getPublicId().equals(request.requiredManagerPositionPublicId())) {
                log.warn("Attempted to create position {} with self-referencing required manager position.", request.name());
                throw new PositionSelfReferenceException("exception.organization.position.self-reference", request.name());
            }

            log.debug("Validating and assigning required manager position with public ID: {}", request.requiredManagerPositionPublicId());
            PositionEntity managerPosition = positionRepository.findByPublicId(request.requiredManagerPositionPublicId())
                .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.position.required-manager-not-found", request.requiredManagerPositionPublicId()));

            log.debug("Assigning cross-area manager position {} to position {} in area {}", managerPosition.getName(), request.name(), area.getName());
            newPosition.setRequiredManagerPosition(managerPosition);
        }

        PositionEntity savedPosition = positionRepository.save(newPosition);
        log.info("Successfully created position with public ID: {}", savedPosition.getPublicId());
        return positionMapper.toResponse(savedPosition);
    }

    @Override
    @Transactional
    public CommandPositionResponse update(UUID publicId, UpdatePositionRequest request) {
        log.info("Attempting to update position with public ID: {}", publicId);

        PositionEntity existingPosition = findByPublicId(publicId);
        AreaEntity area = areaUseCase.findByPublicId(request.areaPublicId());
        positionRepository.findByNameAndAreaId(request.name(), area.getId()).ifPresent(p -> {
            if (!Objects.equals(p.getPublicId(), publicId)) {
                log.warn("Attempted to update position {} with a name that already exists in the area: \'{}' in \'{}'", publicId, request.name(), area.getName());
                throw new UniqueValidationException("exception.organization.position.name-already-exists-in-area", request.name(), area.getName());
            }
        });
        positionMapper.updateEntityFromRequest(request, existingPosition);
        existingPosition.setArea(area);

        if (request.requiredManagerPositionPublicId() != null) {
            if (publicId.equals(request.requiredManagerPositionPublicId())) {
                log.warn("Attempted to update position {} with self-referencing required manager position.", existingPosition.getName());
                throw new PositionSelfReferenceException("exception.organization.position.self-reference", existingPosition.getName());
            }

            log.debug("Validating and updating required manager position for {} to public ID: {}", publicId, request.requiredManagerPositionPublicId());
            PositionEntity managerPosition = positionRepository.findByPublicId(request.requiredManagerPositionPublicId())
                .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.position.required-manager-not-found", request.requiredManagerPositionPublicId()));

            log.debug("Assigning cross-area manager position {} to position {} in area {}", managerPosition.getName(), existingPosition.getName(), area.getName());
            existingPosition.setRequiredManagerPosition(managerPosition);
        } else {
            log.debug("Unsetting required manager position for {}.", publicId);
            existingPosition.setRequiredManagerPosition(null);
        }

        PositionEntity updatedPosition = positionRepository.save(existingPosition);
        log.info("Successfully updated position with public ID: {}", updatedPosition.getPublicId());
        return positionMapper.toResponse(updatedPosition);
    }


    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete position with public ID: {}", publicId);
        PositionEntity positionToDelete = findByPublicId(publicId);
        if (employeeRepository.countByPositionId(positionToDelete.getId()) > 0) {
            log.warn("Attempted to delete position {} which still has associated employees.", publicId);
            throw new ReferentialIntegrityException("exception.organization.position.cannot-delete-has-employees");
        }
        positionRepository.delete(positionToDelete);
        log.info("Successfully deleted position with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionDetailsDTO getByPublicId(UUID publicId) {
        log.info("Fetching details for position with public ID: {}", publicId);
        PositionEntity position = findByPublicId(publicId);
        long employeeCount = employeeRepository.countByPositionId(position.getId());
        return positionMapper.toDetailsDTO(position, employeeCount);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandPositionResponse getCommandResponseByPublicId(UUID publicId) {
        log.info("Fetching command response for position with public ID: {}", publicId);
        PositionEntity position = findByPublicId(publicId);
        return positionMapper.toResponse(position);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<PositionListDTO> findAllPaged(String code, String name, UUID areaPublicId, Pageable pageable) {
        log.info("Fetching paged list of positions with code: \'{}', name: \'{}', areaPublicId: {}, pageable: {}", code, name, areaPublicId, pageable);

        Specification<PositionEntity> spec = (root, q, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (StringUtils.hasText(code)) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (areaPublicId != null) {
                predicates.add(cb.equal(root.get("area").get("publicId"), areaPublicId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PositionEntity> positionPage = positionRepository.findAll(spec, pageable);
        return positionMapper.toPagedDTO(positionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionSelectOptionDTO> getSelectOptions(UUID areaPublicId) {
        log.info("Fetching all positions for select options, filtered by areaPublicId: {}", areaPublicId);

        Specification<PositionEntity> spec = (root, query, cb) -> {
            if (areaPublicId != null) {
                return cb.equal(root.get("area").get("publicId"), areaPublicId);
            }
            return cb.conjunction();
        };

        List<PositionEntity> positions = positionRepository.findAll(spec);
        return positionMapper.toSelectOptionDTOs(positions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionSelectOptionDTO> getManagerSelectOptions() {
        log.info("Fetching all positions for manager select options (cross-area)");

        List<PositionEntity> positions = positionRepository.findAll();
        return positionMapper.toSelectOptionDTOs(positions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionSyncResponse> findAllForSync() {
        log.info("Fetching all positions for mobile sync");

        List<PositionEntity> positions = positionRepository.findAll();
        return positions.stream()
                .map(p -> new PositionSyncResponse(p.getPublicId(), p.getName()))
                .toList();
    }

    @Override
    public PositionEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find position entity with public ID: {}", publicId);
        return positionRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Position with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.organization.position.not-found", publicId);
                });
    }

}
