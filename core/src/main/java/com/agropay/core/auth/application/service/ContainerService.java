package com.agropay.core.auth.application.service;

import com.agropay.core.auth.application.usecase.IContainerUseCase;
import com.agropay.core.auth.domain.ContainerEntity;
import com.agropay.core.auth.mapper.IContainerMapper;
import com.agropay.core.auth.model.container.*;
import com.agropay.core.auth.persistence.IContainerRepository;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
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
public class ContainerService implements IContainerUseCase {

    private final IContainerRepository containerRepository;
    private final IContainerMapper containerMapper;

    @Override
    @Transactional
    public CommandContainerResponse create(CreateContainerRequest request) {
        log.info("Attempting to create a new container with name: {}", request.name());

        containerRepository.findByName(request.name()).ifPresent(c -> {
            log.warn("Attempted to create a container with a name that already exists: '{}'", request.name());
            throw new UniqueValidationException("exception.auth.container.name-already-exists", request.name());
        });

        ContainerEntity newContainer = containerMapper.toEntity(request);
        ContainerEntity savedContainer = containerRepository.save(newContainer);
        log.info("Successfully created container with public ID: {}", savedContainer.getPublicId());
        return containerMapper.toResponse(savedContainer);
    }

    @Override
    @Transactional
    public CommandContainerResponse update(UUID publicId, UpdateContainerRequest request) {
        log.info("Attempting to update container with public ID: {}", publicId);

        ContainerEntity existingContainer = findByPublicId(publicId);
        containerRepository.findByName(request.name()).ifPresent(c -> {
            if (!Objects.equals(c.getPublicId(), publicId)) {
                log.warn("Attempted to update container {} with a name that already exists: '{}'", publicId, request.name());
                throw new UniqueValidationException("exception.auth.container.name-already-exists", request.name());
            }
        });

        containerMapper.updateEntityFromRequest(request, existingContainer);
        ContainerEntity updatedContainer = containerRepository.save(existingContainer);
        log.info("Successfully updated container with public ID: {}", updatedContainer.getPublicId());
        return containerMapper.toResponse(updatedContainer);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete container with public ID: {}", publicId);
        ContainerEntity containerToDelete = findByPublicId(publicId);
        containerRepository.delete(containerToDelete);
        log.info("Successfully deleted container with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public ContainerDetailsDTO getByPublicId(UUID publicId) {
        log.info("Fetching details for container with public ID: {}", publicId);
        ContainerEntity container = findByPublicId(publicId);
        return containerMapper.toDetailsDTO(container);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandContainerResponse getCommandResponseByPublicId(UUID publicId) {
        log.info("Fetching command response for container with public ID: {}", publicId);
        ContainerEntity container = findByPublicId(publicId);
        return containerMapper.toResponse(container);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<ContainerListDTO> findAllPaged(String query, Pageable pageable) {
        log.info("Fetching paged list of containers with query: '{}', pageable: {}", query, pageable);

        Specification<ContainerEntity> spec = (root, q, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (StringUtils.hasText(query)) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("displayName")), searchPattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ContainerEntity> containerPage = containerRepository.findAll(spec, pageable);
        return containerMapper.toPagedDTO(containerPage);
    }

    private ContainerEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find container entity with public ID: {}", publicId);
        return containerRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Container with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.auth.container.not-found", publicId);
                });
    }
}

