package com.agropay.core.auth.application.service;

import com.agropay.core.auth.application.usecase.IElementUseCase;
import com.agropay.core.auth.domain.ContainerEntity;
import com.agropay.core.auth.domain.ElementEntity;
import com.agropay.core.auth.mapper.IElementMapper;
import com.agropay.core.auth.model.element.*;
import com.agropay.core.auth.persistence.IContainerRepository;
import com.agropay.core.auth.persistence.IElementRepository;
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
public class ElementService implements IElementUseCase {

    private final IElementRepository elementRepository;
    private final IContainerRepository containerRepository;
    private final IElementMapper elementMapper;

    @Override
    @Transactional
    public CommandElementResponse create(CreateElementRequest request) {
        log.info("Attempting to create a new element with name: {}", request.name());

        elementRepository.findByName(request.name()).ifPresent(e -> {
            log.warn("Attempted to create an element with a name that already exists: '{}'", request.name());
            throw new UniqueValidationException("exception.auth.element.name-already-exists", request.name());
        });

        ElementEntity newElement = elementMapper.toEntity(request);
        
        if (request.containerPublicId() != null) {
            ContainerEntity container = containerRepository.findByPublicId(request.containerPublicId())
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.container.not-found", request.containerPublicId()));
            newElement.setContainer(container);
        }

        ElementEntity savedElement = elementRepository.save(newElement);
        log.info("Successfully created element with public ID: {}", savedElement.getPublicId());
        return elementMapper.toResponse(savedElement);
    }

    @Override
    @Transactional
    public CommandElementResponse update(UUID publicId, UpdateElementRequest request) {
        log.info("Attempting to update element with public ID: {}", publicId);

        ElementEntity existingElement = findByPublicId(publicId);
        elementRepository.findByName(request.name()).ifPresent(e -> {
            if (!Objects.equals(e.getPublicId(), publicId)) {
                log.warn("Attempted to update element {} with a name that already exists: '{}'", publicId, request.name());
                throw new UniqueValidationException("exception.auth.element.name-already-exists", request.name());
            }
        });

        elementMapper.updateEntityFromRequest(request, existingElement);
        
        if (request.containerPublicId() != null) {
            ContainerEntity container = containerRepository.findByPublicId(request.containerPublicId())
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.container.not-found", request.containerPublicId()));
            existingElement.setContainer(container);
        } else {
            existingElement.setContainer(null);
        }

        ElementEntity updatedElement = elementRepository.save(existingElement);
        log.info("Successfully updated element with public ID: {}", updatedElement.getPublicId());
        return elementMapper.toResponse(updatedElement);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete element with public ID: {}", publicId);
        ElementEntity elementToDelete = findByPublicId(publicId);
        elementRepository.delete(elementToDelete);
        log.info("Successfully deleted element with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public ElementDetailsDTO getByPublicId(UUID publicId) {
        log.info("Fetching details for element with public ID: {}", publicId);
        ElementEntity element = findByPublicId(publicId);
        return elementMapper.toDetailsDTO(element);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandElementResponse getCommandResponseByPublicId(UUID publicId) {
        log.info("Fetching command response for element with public ID: {}", publicId);
        ElementEntity element = findByPublicId(publicId);
        return elementMapper.toResponse(element);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<ElementListDTO> findAllPaged(String query, UUID containerPublicId, Pageable pageable) {
        log.info("Fetching paged list of elements with query: '{}', containerPublicId: {}, pageable: {}", query, containerPublicId, pageable);

        Specification<ElementEntity> spec = (root, q, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (StringUtils.hasText(query)) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("displayName")), searchPattern)
                ));
            }

            if (containerPublicId != null) {
                predicates.add(cb.equal(root.get("container").get("publicId"), containerPublicId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ElementEntity> elementPage = elementRepository.findAll(spec, pageable);
        return elementMapper.toPagedDTO(elementPage);
    }

    private ElementEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find element entity with public ID: {}", publicId);
        return elementRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Element with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.auth.element.not-found", publicId);
                });
    }
}

