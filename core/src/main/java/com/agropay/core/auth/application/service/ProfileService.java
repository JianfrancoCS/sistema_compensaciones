package com.agropay.core.auth.application.service;

import com.agropay.core.auth.application.usecase.IProfileUseCase;
import com.agropay.core.auth.domain.ElementEntity;
import com.agropay.core.auth.domain.ProfileElementEntity;
import com.agropay.core.auth.domain.ProfileEntity;
import com.agropay.core.auth.mapper.IProfileMapper;
import com.agropay.core.auth.model.profile.*;
import com.agropay.core.auth.persistence.IContainerRepository;
import com.agropay.core.auth.persistence.IElementRepository;
import com.agropay.core.auth.persistence.IProfileElementRepository;
import com.agropay.core.auth.persistence.IProfileRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService implements IProfileUseCase {

    private final IProfileRepository profileRepository;
    private final IProfileElementRepository profileElementRepository;
    private final IElementRepository elementRepository;
    private final IContainerRepository containerRepository;
    private final IProfileMapper profileMapper;

    @Override
    @Transactional
    public CommandProfileResponse create(CreateProfileRequest request) {
        log.info("Attempting to create a new profile with name: {}", request.name());

        profileRepository.findByName(request.name()).ifPresent(p -> {
            log.warn("Attempted to create a profile with a name that already exists: '{}'", request.name());
            throw new UniqueValidationException("exception.auth.profile.name-already-exists", request.name());
        });

        ProfileEntity newProfile = profileMapper.toEntity(request);
        ProfileEntity savedProfile = profileRepository.save(newProfile);
        log.info("Successfully created profile with public ID: {}", savedProfile.getPublicId());
        return profileMapper.toResponse(savedProfile);
    }

    @Override
    @Transactional
    public CommandProfileResponse update(UUID publicId, UpdateProfileRequest request) {
        log.info("Attempting to update profile with public ID: {}", publicId);

        ProfileEntity existingProfile = findByPublicId(publicId);
        profileRepository.findByName(request.name()).ifPresent(p -> {
            if (!Objects.equals(p.getPublicId(), publicId)) {
                log.warn("Attempted to update profile {} with a name that already exists: '{}'", publicId, request.name());
                throw new UniqueValidationException("exception.auth.profile.name-already-exists", request.name());
            }
        });

        profileMapper.updateEntityFromRequest(request, existingProfile);
        ProfileEntity updatedProfile = profileRepository.save(existingProfile);
        log.info("Successfully updated profile with public ID: {}", updatedProfile.getPublicId());
        return profileMapper.toResponse(updatedProfile);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete profile with public ID: {}", publicId);
        ProfileEntity profileToDelete = findByPublicId(publicId);
        profileRepository.delete(profileToDelete);
        log.info("Successfully deleted profile with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDetailsDTO getByPublicId(UUID publicId) {
        log.info("Fetching details for profile with public ID: {}", publicId);
        ProfileEntity profile = findByPublicId(publicId);
        return profileMapper.toDetailsDTO(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandProfileResponse getCommandResponseByPublicId(UUID publicId) {
        log.info("Fetching command response for profile with public ID: {}", publicId);
        ProfileEntity profile = findByPublicId(publicId);
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<ProfileListDTO> findAllPaged(String query, Pageable pageable) {
        log.info("Fetching paged list of profiles with query: '{}', pageable: {}", query, pageable);

        Specification<ProfileEntity> spec = (root, q, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (StringUtils.hasText(query)) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProfileEntity> profilePage = profileRepository.findAll(spec, pageable);
        return profileMapper.toPagedDTO(profilePage);
    }

    @Override
    @Transactional
    public void assignElements(UUID profilePublicId, AssignElementsRequest request) {
        log.info("Attempting to assign elements to profile with public ID: {}", profilePublicId);

        ProfileEntity profile = findByPublicId(profilePublicId);

        // Soft delete existing assignments
        profileElementRepository.softDeleteByProfileId(profile.getId());

        // Create new assignments
        for (UUID elementPublicId : request.elementPublicIds()) {
            ElementEntity element = elementRepository.findByPublicId(elementPublicId)
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.element.not-found", elementPublicId));

            // Check if assignment already exists (not deleted)
            Optional<ProfileElementEntity> existing = profileElementRepository
                    .findByProfileIdAndElementId(profile.getId(), element.getId());

            if (existing.isPresent()) {
                // Reactivate if it was soft deleted
                ProfileElementEntity pe = existing.get();
                if (pe.getDeletedAt() != null) {
                    pe.setDeletedAt(null);
                    profileElementRepository.save(pe);
                }
            } else {
                // Create new assignment
                ProfileElementEntity profileElement = new ProfileElementEntity();
                profileElement.setProfile(profile);
                profileElement.setElement(element);
                profileElementRepository.save(profileElement);
            }
        }

        log.info("Successfully assigned {} elements to profile {}", request.elementPublicIds().size(), profilePublicId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileElementsByContainerDTO getElementsByContainer(UUID profilePublicId) {
        log.info("Fetching elements by container for profile with public ID: {}", profilePublicId);

        ProfileEntity profile = findByPublicId(profilePublicId);

        // Get all containers with their elements
        List<com.agropay.core.auth.domain.ContainerEntity> allContainers = 
                containerRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByOrderIndex();

        // Get assigned elements for this profile
        List<ProfileElementEntity> profileElements = profileElementRepository.findByProfileId(profile.getId());
        Set<UUID> assignedElementPublicIds = profileElements.stream()
                .map(pe -> pe.getElement().getPublicId())
                .collect(Collectors.toSet());

        // Build the response structure
        List<ProfileElementsByContainerDTO.ContainerWithElements> containersWithElements = new ArrayList<>();

        // Get all active elements once
        List<ElementEntity> allActiveElements = elementRepository.findAllActive();
        
        for (com.agropay.core.auth.domain.ContainerEntity container : allContainers) {
            // Get all elements for this container
            List<ElementEntity> containerElements = allActiveElements.stream()
                    .filter(e -> e.getContainer() != null && e.getContainer().getId().equals(container.getId()))
                    .sorted(Comparator.comparing(ElementEntity::getOrderIndex))
                    .collect(Collectors.toList());

            // Get selected element IDs for this container
            List<UUID> selectedElementPublicIds = containerElements.stream()
                    .filter(e -> assignedElementPublicIds.contains(e.getPublicId()))
                    .map(ElementEntity::getPublicId)
                    .collect(Collectors.toList());

            // Map elements to DTOs
            List<ProfileElementsByContainerDTO.ElementInfo> elementInfos = containerElements.stream()
                    .map(e -> new ProfileElementsByContainerDTO.ElementInfo(
                            e.getPublicId(),
                            e.getName(),
                            e.getDisplayName(),
                            e.getRoute(),
                            e.getIcon()
                    ))
                    .collect(Collectors.toList());

            containersWithElements.add(new ProfileElementsByContainerDTO.ContainerWithElements(
                    container.getPublicId(),
                    container.getName(),
                    container.getDisplayName(),
                    container.getIcon(),
                    elementInfos,
                    selectedElementPublicIds
            ));
        }

        // Also include elements without container
        List<ElementEntity> elementsWithoutContainer = allActiveElements.stream()
                .filter(e -> e.getContainer() == null)
                .sorted(Comparator.comparing(ElementEntity::getOrderIndex))
                .collect(Collectors.toList());

        if (!elementsWithoutContainer.isEmpty()) {
            List<UUID> selectedElementPublicIds = elementsWithoutContainer.stream()
                    .filter(e -> assignedElementPublicIds.contains(e.getPublicId()))
                    .map(ElementEntity::getPublicId)
                    .collect(Collectors.toList());

            List<ProfileElementsByContainerDTO.ElementInfo> elementInfos = elementsWithoutContainer.stream()
                    .map(e -> new ProfileElementsByContainerDTO.ElementInfo(
                            e.getPublicId(),
                            e.getName(),
                            e.getDisplayName(),
                            e.getRoute(),
                            e.getIcon()
                    ))
                    .collect(Collectors.toList());

            containersWithElements.add(new ProfileElementsByContainerDTO.ContainerWithElements(
                    null,
                    "Sin Contenedor",
                    "Elementos sin contenedor",
                    null,
                    elementInfos,
                    selectedElementPublicIds
            ));
        }

        return new ProfileElementsByContainerDTO(
                profile.getPublicId(),
                profile.getName(),
                containersWithElements
        );
    }

    private ProfileEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find profile entity with public ID: {}", publicId);
        return profileRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Profile with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.auth.profile.not-found", publicId);
                });
    }
}

