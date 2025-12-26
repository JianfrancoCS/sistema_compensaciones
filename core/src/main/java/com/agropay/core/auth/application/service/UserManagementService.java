package com.agropay.core.auth.application.service;

import com.agropay.core.auth.application.usecase.IUserManagementUseCase;
import com.agropay.core.auth.domain.ContainerEntity;
import com.agropay.core.auth.domain.ElementEntity;
import com.agropay.core.auth.domain.ProfileElementEntity;
import com.agropay.core.auth.domain.ProfileEntity;
import com.agropay.core.auth.domain.UserEntity;
import com.agropay.core.auth.domain.UserProfileEntity;
import com.agropay.core.auth.model.user.AssignElementsRequest;
import com.agropay.core.auth.model.user.CreateUserRequest;
import com.agropay.core.auth.model.user.ProfileForAssignmentDTO;
import com.agropay.core.auth.model.user.SyncUserProfilesRequest;
import com.agropay.core.auth.model.user.UpdateUserStatusRequest;
import com.agropay.core.auth.model.user.UserDetailsDTO;
import com.agropay.core.auth.model.user.UserElementsByContainerDTO;
import com.agropay.core.auth.model.user.UserListDTO;
import com.agropay.core.auth.constant.BaseProfileEnum;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.auth.persistence.*;
import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.hiring.persistence.IContractRepository;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.organization.persistence.IEmployeeRepository;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.utils.PagedResult;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService implements IUserManagementUseCase {

    private final IUserRepository userRepository;
    private final IUserProfileRepository userProfileRepository;
    private final IProfileElementRepository profileElementRepository;
    private final IElementRepository elementRepository;
    private final IContainerRepository containerRepository;
    private final IProfileRepository profileRepository;
    private final IEmployeeRepository employeeRepository;
    private final IContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UUID create(CreateUserRequest request) {
        log.info("Creating new user with username: {}, employeeId: {}, positionId: {}", 
                request.username(), request.employeeId(), request.positionId());

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessValidationException("exception.auth.user.username-already-exists", 
                    String.format("El nombre de usuario '%s' ya existe en el sistema", request.username()));
        }

        if (request.employeeId() != null && !request.employeeId().isBlank()) {
            Optional<EmployeeEntity> employeeOpt = employeeRepository.findByPersonDocumentNumber(request.employeeId());
            if (employeeOpt.isEmpty()) {
                throw new BusinessValidationException("exception.organization.employee.not-found", 
                        String.format("No se encontró un empleado con el número de documento '%s'", request.employeeId()));
            }

            if (request.positionId() != null) {
                EmployeeEntity employee = employeeOpt.get();
                if (employee.getPosition() == null || !employee.getPosition().getPublicId().equals(request.positionId())) {
                    throw new BusinessValidationException("exception.organization.employee.position-mismatch", 
                            String.format("El empleado con documento '%s' no tiene el cargo especificado", request.employeeId()));
                }
            }
        }

        ProfileEntity colaboradorProfile = profileRepository.findByName(BaseProfileEnum.COLABORADOR.getName())
                .orElseThrow(() -> new BusinessValidationException("exception.auth.profile.colaborador-not-found", 
                        String.format("El perfil base '%s' no existe en el sistema. Por favor, asegúrese de que la migración V116 se haya ejecutado correctamente.", 
                                BaseProfileEnum.COLABORADOR.getName())));

        UserEntity newUser = new UserEntity();
        newUser.setUsername(request.username());
        newUser.setEmployeeId(request.employeeId() != null && !request.employeeId().isBlank() ? request.employeeId() : null);
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));
        newUser.setProfileId(colaboradorProfile.getId());
        newUser.setIsActive(true);

        UserEntity savedUser = userRepository.save(newUser);
        log.info("User created with publicId: {}, username: {}, employeeId: {}", 
                savedUser.getPublicId(), savedUser.getUsername(), savedUser.getEmployeeId());

        Optional<UserProfileEntity> existingUserProfile = userProfileRepository.findByUserIdAndProfileId(
                savedUser.getId(), colaboradorProfile.getId());

        if (existingUserProfile.isEmpty()) {
            UserProfileEntity userProfile = new UserProfileEntity();
            userProfile.setUser(savedUser);
            userProfile.setProfile(colaboradorProfile);
            userProfile.setIsActive(true);
            userProfileRepository.save(userProfile);
            log.info("Assigned '{}' profile as additional profile to user {}", 
                    BaseProfileEnum.COLABORADOR.getName(), savedUser.getUsername());
        }

        log.info("User creation completed. Username: {}, publicId: {}", 
                savedUser.getUsername(), savedUser.getPublicId());

        return savedUser.getPublicId();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<UserListDTO> findAllPaged(String search, Boolean isActive, UUID positionId, Pageable pageable) {
        log.info("Fetching paged list of users with search: '{}', isActive: {}, positionId: {}, pageable: {}", search, isActive, positionId, pageable);

        Specification<UserEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("username")), searchPattern),
                    cb.like(cb.lower(root.get("employeeId")), searchPattern)
                ));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (positionId != null) {
                jakarta.persistence.criteria.Subquery<String> subquery = query.subquery(String.class);
                jakarta.persistence.criteria.Root<EmployeeEntity> employeeRoot = subquery.from(EmployeeEntity.class);
                jakarta.persistence.criteria.Join<EmployeeEntity, PositionEntity> positionJoin = employeeRoot.join("position", JoinType.INNER);
                subquery.select(employeeRoot.get("personDocumentNumber"));
                subquery.where(cb.and(
                    cb.equal(positionJoin.get("publicId"), positionId),
                    cb.isNull(employeeRoot.get("deletedAt"))
                ));
                predicates.add(cb.in(root.get("employeeId")).value(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<UserEntity> userPage = userRepository.findAll(spec, pageable);
        
        List<UserListDTO> userDTOs = userPage.getContent().stream()
            .map(user -> {
                UUID positionPublicId = null;
                String positionName = null;
                
                if (user.getEmployeeId() != null) {
                    Optional<EmployeeEntity> employee = employeeRepository.findByPersonDocumentNumber(user.getEmployeeId());
                    if (employee.isPresent() && employee.get().getPosition() != null) {
                        PositionEntity position = employee.get().getPosition();
                        positionPublicId = position.getPublicId();
                        positionName = position.getName();
                    }
                }
                
                return new UserListDTO(
                    user.getPublicId(),
                    user.getUsername(),
                    user.getEmployeeId(),
                    positionPublicId,
                    positionName,
                    user.getIsActive(),
                    user.getLastLoginAt(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                );
            })
            .collect(Collectors.toList());

        return new PagedResult<>(
            userDTOs,
            userPage.getTotalElements(),
            userPage.getNumber(),
            userPage.getTotalPages(),
            userPage.isFirst(),
            userPage.isLast(),
            userPage.hasNext(),
            userPage.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserElementsByContainerDTO getElementsByContainer(UUID userPublicId) {
        log.info("Fetching elements by container for user with public ID: {}", userPublicId);

        UserEntity user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.user.not-found", userPublicId));

        List<Short> activeProfileIds = new ArrayList<>();
        
        if (user.getProfileId() != null) {
            activeProfileIds.add(user.getProfileId());
        }
        
        userProfileRepository.findActiveProfilesByUserId(user.getId()).forEach(userProfile -> {
            Short profileId = userProfile.getProfile().getId();
            if (!activeProfileIds.contains(profileId)) {
                activeProfileIds.add(profileId);
            }
        });

        Set<UUID> assignedElementPublicIds = new HashSet<>();
        for (Short profileId : activeProfileIds) {
            List<ProfileElementEntity> profileElements = profileElementRepository.findByProfileId(profileId);
            profileElements.forEach(pe -> assignedElementPublicIds.add(pe.getElement().getPublicId()));
        }

        List<ContainerEntity> allContainers = 
                containerRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByOrderIndex();

        List<ElementEntity> allActiveElements = elementRepository.findAllActive();

        List<UserElementsByContainerDTO.ContainerWithElements> containersWithElements = new ArrayList<>();

        for (ContainerEntity container : allContainers) {
            List<ElementEntity> containerElements = allActiveElements.stream()
                    .filter(e -> e.getContainer() != null && e.getContainer().getId().equals(container.getId()))
                    .sorted(Comparator.comparing(ElementEntity::getOrderIndex))
                    .collect(Collectors.toList());

            List<UUID> selectedElementPublicIds = containerElements.stream()
                    .filter(e -> assignedElementPublicIds.contains(e.getPublicId()))
                    .map(ElementEntity::getPublicId)
                    .collect(Collectors.toList());

            List<UserElementsByContainerDTO.ElementInfo> elementInfos = containerElements.stream()
                    .map(e -> new UserElementsByContainerDTO.ElementInfo(
                            e.getPublicId(),
                            e.getName(),
                            e.getDisplayName(),
                            e.getRoute(),
                            e.getIcon()
                    ))
                    .collect(Collectors.toList());

            containersWithElements.add(new UserElementsByContainerDTO.ContainerWithElements(
                    container.getPublicId(),
                    container.getName(),
                    container.getDisplayName(),
                    container.getIcon(),
                    elementInfos,
                    selectedElementPublicIds
            ));
        }

        List<ElementEntity> elementsWithoutContainer = allActiveElements.stream()
                .filter(e -> e.getContainer() == null)
                .sorted(Comparator.comparing(ElementEntity::getOrderIndex))
                .collect(Collectors.toList());

        if (!elementsWithoutContainer.isEmpty()) {
            List<UUID> selectedElementPublicIds = elementsWithoutContainer.stream()
                    .filter(e -> assignedElementPublicIds.contains(e.getPublicId()))
                    .map(ElementEntity::getPublicId)
                    .collect(Collectors.toList());

            List<UserElementsByContainerDTO.ElementInfo> elementInfos = elementsWithoutContainer.stream()
                    .map(e -> new UserElementsByContainerDTO.ElementInfo(
                            e.getPublicId(),
                            e.getName(),
                            e.getDisplayName(),
                            e.getRoute(),
                            e.getIcon()
                    ))
                    .collect(Collectors.toList());

            containersWithElements.add(new UserElementsByContainerDTO.ContainerWithElements(
                    null,
                    "Sin Contenedor",
                    "Elementos sin contenedor",
                    null,
                    elementInfos,
                    selectedElementPublicIds
            ));
        }

        return new UserElementsByContainerDTO(
                user.getPublicId(),
                user.getUsername(),
                containersWithElements
        );
    }

    @Override
    @Transactional
    public void assignElements(UUID userPublicId, AssignElementsRequest request) {
        log.info("Attempting to assign elements to user with public ID: {}", userPublicId);

        UserEntity user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.user.not-found", userPublicId));

       
        String customProfileName = "Personalizado-" + user.getUsername();
        
        com.agropay.core.auth.domain.ProfileEntity customProfile = profileRepository.findByName(customProfileName)
                .orElse(null);

        if (customProfile == null) {
            customProfile = new com.agropay.core.auth.domain.ProfileEntity();
            customProfile.setName(customProfileName);
            customProfile.setDescription("Perfil personalizado para " + user.getUsername());
            customProfile.setIsActive(true);
            customProfile = profileRepository.save(customProfile);
            log.info("Created custom profile {} for user {}", customProfileName, user.getUsername());
        } else {
            profileElementRepository.softDeleteByProfileId(customProfile.getId());
            log.info("Clearing existing elements from custom profile {} for user {}", customProfileName, user.getUsername());
        }

        for (UUID elementPublicId : request.elementPublicIds()) {
            ElementEntity element = elementRepository.findByPublicId(elementPublicId)
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.element.not-found", elementPublicId));

            Optional<ProfileElementEntity> existing = profileElementRepository
                    .findByProfileIdAndElementId(customProfile.getId(), element.getId());

            if (existing.isPresent()) {
                ProfileElementEntity pe = existing.get();
                if (pe.getDeletedAt() != null) {
                    pe.setDeletedAt(null);
                    profileElementRepository.save(pe);
                }
            } else {
                ProfileElementEntity profileElement = new ProfileElementEntity();
                profileElement.setProfile(customProfile);
                profileElement.setElement(element);
                profileElementRepository.save(profileElement);
            }
        }

        Optional<UserProfileEntity> existingUserProfile = userProfileRepository
                .findByUserIdAndProfileId(user.getId(), customProfile.getId());

        if (existingUserProfile.isEmpty()) {
            UserProfileEntity userProfile = new UserProfileEntity();
            userProfile.setUser(user);
            userProfile.setProfile(customProfile);
            userProfile.setIsActive(true);
            userProfileRepository.save(userProfile);
            log.info("Assigned custom profile {} to user {}", customProfileName, user.getUsername());
        } else {
            UserProfileEntity userProfile = existingUserProfile.get();
            if (!userProfile.getIsActive() || userProfile.getDeletedAt() != null) {
                userProfile.setIsActive(true);
                userProfile.setDeletedAt(null);
                userProfileRepository.save(userProfile);
                log.info("Reactivated custom profile {} for user {}", customProfileName, user.getUsername());
            }
        }

        log.info("Successfully assigned {} elements to user {} through custom profile", 
                request.elementPublicIds().size(), user.getUsername());
        
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileForAssignmentDTO> getProfilesForAssignment(UUID userPublicId) {
        log.info("Fetching profiles for assignment for user with public ID: {}", userPublicId);

        UserEntity user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.user.not-found", userPublicId));

        List<ProfileEntity> allActiveProfiles = profileRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByName();

        Set<Short> assignedProfileIds = new HashSet<>();
        
        if (user.getProfileId() != null) {
            assignedProfileIds.add(user.getProfileId());
        }
        
        userProfileRepository.findAllProfilesByUserId(user.getId()).forEach(userProfile -> {
            Short profileId = userProfile.getProfile().getId();
            assignedProfileIds.add(profileId);
        });

        List<ProfileForAssignmentDTO> profilesForAssignment = allActiveProfiles.stream()
                .map(profile -> new ProfileForAssignmentDTO(
                        profile.getPublicId(),
                        profile.getName(),
                        profile.getDescription(),
                        assignedProfileIds.contains(profile.getId()),
                        user.getUsername() 
                ))
                .collect(Collectors.toList());

        log.info("Found {} profiles for assignment, {} are selected", 
                profilesForAssignment.size(), 
                profilesForAssignment.stream().filter(ProfileForAssignmentDTO::isSelected).count());

        return profilesForAssignment;
    }

    @Override
    @Transactional
    public void syncUserProfiles(UUID userPublicId, SyncUserProfilesRequest request) {
        log.info("Synchronizing profiles for user with public ID: {}, profile IDs: {}", 
                userPublicId, request.profileIds());

        UserEntity user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.user.not-found", userPublicId));

        List<UserProfileEntity> existingUserProfiles = userProfileRepository.findAllProfilesByUserId(user.getId());
        for (UserProfileEntity userProfile : existingUserProfiles) {
            userProfile.setIsActive(false);
            userProfile.setDeletedAt(java.time.LocalDateTime.now());
            userProfileRepository.save(userProfile);
        }

        if (user.getProfileId() != null) {
            ProfileEntity currentProfile = profileRepository.findById(user.getProfileId()).orElse(null);
            if (currentProfile != null) {
                boolean shouldKeepProfile = request.profileIds().contains(currentProfile.getPublicId());
                if (!shouldKeepProfile) {
                    user.setProfileId(null);
                    userRepository.save(user);
                    log.info("Removed main profile {} from user {}", currentProfile.getName(), user.getUsername());
                }
            }
        }

        Set<Short> profileIdsToAssign = new HashSet<>();
        for (UUID profilePublicId : request.profileIds()) {
            ProfileEntity profile = profileRepository.findByPublicId(profilePublicId)
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.profile.not-found", profilePublicId));

            profileIdsToAssign.add(profile.getId());
        }

        Short mainProfileId = null;
        if (!request.profileIds().isEmpty()) {
            UUID firstProfilePublicId = request.profileIds().get(0);
            ProfileEntity firstProfile = profileRepository.findByPublicId(firstProfilePublicId)
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.profile.not-found", firstProfilePublicId));
            mainProfileId = firstProfile.getId();
            
            user.setProfileId(mainProfileId);
            userRepository.save(user);
            log.info("Set main profile {} for user {}", firstProfile.getName(), user.getUsername());
        }

        for (UUID profilePublicId : request.profileIds()) {
            ProfileEntity profile = profileRepository.findByPublicId(profilePublicId)
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.profile.not-found", profilePublicId));

            if (profile.getId().equals(mainProfileId)) {
                continue;
            }

            Optional<UserProfileEntity> existingUserProfile = userProfileRepository
                    .findByUserIdAndProfileId(user.getId(), profile.getId());

            if (existingUserProfile.isPresent()) {
                UserProfileEntity userProfile = existingUserProfile.get();
                userProfile.setIsActive(true);
                userProfile.setDeletedAt(null);
                userProfileRepository.save(userProfile);
                log.info("Reactivated profile {} for user {}", profile.getName(), user.getUsername());
            } else {
                UserProfileEntity userProfile = new UserProfileEntity();
                userProfile.setUser(user);
                userProfile.setProfile(profile);
                userProfile.setIsActive(true);
                userProfileRepository.save(userProfile);
                log.info("Assigned profile {} to user {}", profile.getName(), user.getUsername());
            }
        }

        log.info("Successfully synchronized {} profiles for user {}", 
                request.profileIds().size(), user.getUsername());
    }

    @Override
    @Transactional
    public void updateUserStatus(UUID userPublicId, UpdateUserStatusRequest request) {
        log.info("Updating user status for user with public ID: {}, isActive: {}", userPublicId, request.isActive());

        UserEntity user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.user.not-found", userPublicId));

        user.setIsActive(request.isActive());
        userRepository.save(user);

        log.info("User {} status updated to {}", user.getUsername(), request.isActive() ? "ACTIVE" : "INACTIVE");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsDTO getUserDetails(UUID userPublicId) {
        log.info("Fetching user details for user with public ID: {}", userPublicId);

        UserEntity user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.auth.user.not-found", userPublicId));

        UserDetailsDTO.EmployeeInfo employeeInfo = null;
        UserDetailsDTO.ContractInfo contractInfo = null;

        if (user.getEmployeeId() != null) {
            Optional<EmployeeEntity> employeeOpt = employeeRepository.findByPersonDocumentNumber(user.getEmployeeId());
            
            if (employeeOpt.isPresent()) {
                EmployeeEntity employee = employeeOpt.get();
                PersonEntity person = employee.getPerson();
                PositionEntity position = employee.getPosition();
                SubsidiaryEntity subsidiary = employee.getSubsidiary();
                StateEntity state = employee.getState();

                employeeInfo = new UserDetailsDTO.EmployeeInfo(
                        employee.getCode(),
                        employee.getPersonDocumentNumber(),
                        person != null ? person.getNames() : null,
                        person != null ? person.getPaternalLastname() : null,
                        person != null ? person.getMaternalLastname() : null,
                        person != null ? person.getDob() : null,
                        person != null ? person.getGender() : null,
                        position != null ? position.getPublicId() : null,
                        position != null ? position.getName() : null,
                        position != null ? position.getSalary() : null,
                        employee.getCustomSalary(),
                        employee.getDailyBasicSalary(),
                        employee.getHireDate(),
                        subsidiary != null ? subsidiary.getName() : null,
                        state != null ? state.getName() : null,
                        employee.getAfpAffiliationNumber(),
                        employee.getBankAccountNumber(),
                        employee.getBankName()
                );

                Optional<ContractEntity> contractOpt = contractRepository.findByPersonDocumentNumber(user.getEmployeeId());
                
                if (contractOpt.isPresent()) {
                    ContractEntity contract = contractOpt.get();
                    if (contract.getDeletedAt() == null) {
                        contractInfo = new UserDetailsDTO.ContractInfo(
                                contract.getPublicId(),
                                contract.getContractNumber(),
                                contract.getStartDate(),
                                contract.getEndDate(),
                                contract.getExtendedEndDate(),
                                contract.getContractType() != null ? contract.getContractType().getName() : null,
                                contract.getState() != null ? contract.getState().getName() : null
                        );
                    }
                } else {
                    List<ContractEntity> contracts = contractRepository.findAll((root, query, cb) -> {
                        return cb.and(
                                cb.equal(root.get("personDocumentNumber"), user.getEmployeeId()),
                                cb.isNull(root.get("deletedAt"))
                        );
                    });

                    if (!contracts.isEmpty()) {
                        ContractEntity contract = contracts.stream()
                                .max((c1, c2) -> c1.getStartDate().compareTo(c2.getStartDate()))
                                .orElse(null);

                        if (contract != null) {
                            contractInfo = new UserDetailsDTO.ContractInfo(
                                    contract.getPublicId(),
                                    contract.getContractNumber(),
                                    contract.getStartDate(),
                                    contract.getEndDate(),
                                    contract.getExtendedEndDate(),
                                    contract.getContractType() != null ? contract.getContractType().getName() : null,
                                    contract.getState() != null ? contract.getState().getName() : null
                            );
                        }
                    }
                }
            }
        }

        return new UserDetailsDTO(
                user.getPublicId(),
                user.getUsername(),
                user.getIsActive(),
                employeeInfo,
                contractInfo
        );
    }
}

