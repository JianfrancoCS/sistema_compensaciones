package com.agropay.core.organization.application.services;

import com.agropay.core.images.application.usecase.IImageUseCase;
import com.agropay.core.images.domain.ImageEntity;
import com.agropay.core.organization.application.usecase.IDocumentTypeUseCase;
import com.agropay.core.organization.application.usecase.IEmployeeUseCase;
import com.agropay.core.organization.application.usecase.IPersonUseCase;
import com.agropay.core.organization.application.usecase.IPositionUseCase;
import com.agropay.core.organization.application.usecase.ISubsidiaryUseCase;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.persistence.IConceptRepository;
import com.agropay.core.organization.exception.*;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.organization.mapper.IEmployeeMapper;
import com.agropay.core.organization.model.employee.*;
import com.agropay.core.organization.persistence.EmployeeSpecification;
import com.agropay.core.organization.persistence.IEmployeeRepository;
import com.agropay.core.states.application.IStateUseCase;
import com.agropay.core.organization.api.EmployeeStateEnum;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.exceptions.NoChangesDetectedException;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService implements IEmployeeUseCase {
    private final IEmployeeRepository employeeRepository;
    private final IEmployeeMapper employeeMapper;
    private final IPersonUseCase personService;
    private final ISubsidiaryUseCase subsidiaryService;
    private final IPositionUseCase positionService;
    private final IStateUseCase stateService;
    private final IDocumentTypeUseCase documentTypeUseCase;
    private final IImageUseCase imageUseCase;
    private final IConceptRepository conceptRepository;

    @Override
    @Transactional
    public CommandEmployeeResponse create(CreateEmployeeRequest request) {
        log.info("Attempting to create a new employee for document number: {}", request.documentNumber());
        if (employeeRepository.existsById(request.documentNumber())) {
            throw new UniqueValidationException("exception.organization.employee.already-exists", request.documentNumber());
        }
        
        // Detectar si es DNI (8 dígitos) o CE (9 dígitos) y buscar/crear persona apropiadamente
        PersonEntity person;
        if (request.documentNumber().length() == 8) {
            // DNI - buscar/crear usando RENIEC
            person = personService.findOrCreatePersonByDni(request.documentNumber());
        } else {
            // CE u otro documento - buscar solo en BD local (debe existir previamente)
            // Si no existe, lanzará excepción (la persona debe ser buscada/creada desde el frontend primero)
            person = personService.findPersonByDocumentNumber(request.documentNumber(), false);
        }

        SubsidiaryEntity subsidiary = subsidiaryService.findByPublicId(request.subsidiaryPublicId());
        PositionEntity position = positionService.findByPublicId(request.positionPublicId());

        StateEntity createdState = stateService.findByCodeAndDomainName(EmployeeStateEnum.CREADO.getCode(), EmployeeEntity.TABLE_NAME);

        EmployeeEntity employee = new EmployeeEntity();
        employee.setPersonDocumentNumber(person.getDocumentNumber());
        employee.setPerson(person);
        employee.setSubsidiary(subsidiary);
        employee.setPosition(position);
        employee.setState(createdState);

        // Asignar conceptos de jubilación y seguro si se proporcionan
        if (request.retirementConceptPublicId() != null) {
            ConceptEntity retirementConcept = conceptRepository.findByPublicId(request.retirementConceptPublicId())
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.concept.not-found", request.retirementConceptPublicId().toString()));
            employee.setRetirementConcept(retirementConcept);
        }

        if (request.healthInsuranceConceptPublicId() != null) {
            ConceptEntity healthInsuranceConcept = conceptRepository.findByPublicId(request.healthInsuranceConceptPublicId())
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.concept.not-found", request.healthInsuranceConceptPublicId().toString()));
            employee.setHealthInsuranceConcept(healthInsuranceConcept);
        }

        EmployeeEntity savedEmployee = employeeRepository.save(employee);
        log.info("Successfully created employee with code: {} in CREATED state", savedEmployee.getCode());
        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional
    public CommandEmployeeResponse update(UUID code, UpdateEmployeeRequest request) {
        log.info("Attempting to update employee with code: {}", code);
        EmployeeEntity employee = findEmployeeByCode(code);

        if (isUpdateRedundant(request, employee)) {
            throw new NoChangesDetectedException("exception.shared.no-changes-detected");
        }

        if (request.subsidiaryPublicId() != null) {
            SubsidiaryEntity subsidiary = subsidiaryService.findByPublicId(request.subsidiaryPublicId());
            employee.setSubsidiary(subsidiary);
        }

        if (request.positionPublicId() != null) {
            PositionEntity newPosition = positionService.findByPublicId(request.positionPublicId());
            employee.setPosition(newPosition);
        }

        if (request.managerCode() != null) {
            EmployeeEntity manager = findEmployeeByCode(request.managerCode());
            employee.setManager(manager);
        } else {
            // Validar que no se pueda crear un segundo CEO
            if (employeeRepository.existsCeoEmployee()) {
                throw new UniqueValidationException("exception.organization.employee.ceo-already-exists");
            }
            employee.setManager(null);
        }

        EmployeeEntity updatedEmployee = employeeRepository.save(employee);
        log.info("Successfully updated employee with code: {}", updatedEmployee.getCode());
        return employeeMapper.toResponse(updatedEmployee);
    }

    @Override
    @Transactional
    public void activateEmployee(UUID code) {
        log.info("Attempting to activate employee with code: {}", code);
        EmployeeEntity employee = findEmployeeByCode(code);

        StateEntity currentState = employee.getState();
        if (currentState == null || !EmployeeStateEnum.CREADO.getCode().equals(currentState.getCode())) {
            String currentStateCode = currentState != null ? currentState.getCode() : "NONE";
            throw new InvalidEmployeeStateException("exception.organization.employee.invalid-state-for-activation",
                    currentStateCode, EmployeeStateEnum.CREADO.getCode());
        }

        PositionEntity position = employee.getPosition();
        if (position.isRequiresManager() && employee.getManager() == null) {
            throw new BusinessValidationException("exception.organization.employee.manager-required", position.getName());
        }

        if (employee.getManager() != null) {
            EmployeeEntity manager = employee.getManager();
            if (position.getRequiredManagerPosition() != null && !Objects.equals(manager.getPosition().getId(), position.getRequiredManagerPosition().getId())) {
                throw new BusinessValidationException("exception.organization.employee.incorrect-manager-position", manager.getPosition().getName(), position.getRequiredManagerPosition().getName());
            }
        }

        StateEntity activeState = stateService.findByCodeAndDomainName(EmployeeStateEnum.ACTIVO.getCode(), EmployeeEntity.TABLE_NAME);
        employee.setState(activeState);
        employeeRepository.save(employee);
        log.info("Successfully activated employee with code: {}", code);
    }

    @Override
    @Transactional
    public void updateSubsidiary(UUID employeePublicId, UpdateEmployeeSubsidiaryRequest request) {
        log.info("Attempting to update subsidiary for employee with public ID: {}", employeePublicId);
        EmployeeEntity employee = findEmployeeByCode(employeePublicId);
        SubsidiaryEntity newSubsidiary = subsidiaryService.findByPublicId(request.newSubsidiaryPublicId());
        employee.setSubsidiary(newSubsidiary);
        employeeRepository.save(employee);
        log.info("Successfully updated subsidiary for employee with public ID: {}", employeePublicId);
    }

    @Override
    @Transactional
    public void updatePosition(UUID employeePublicId, UpdateEmployeePositionRequest request) {
        log.info("Attempting to update position for employee with public ID: {}", employeePublicId);
        EmployeeEntity employee = findEmployeeByCode(employeePublicId);
        PositionEntity newPosition = positionService.findByPublicId(request.newPositionPublicId());

        handlePositionChange(employee, newPosition, request.newManagerCode());

        employeeRepository.save(employee);
        log.info("Successfully updated position for employee with public ID: {}", employeePublicId);
    }

    @Override
    @Transactional
    public void deactivateEmployee(UUID code) {
        log.info("Attempting to delete employee with code: {}", code);
        EmployeeEntity employeeToDelete = findEmployeeByCode(code);

        long subordinateCount = employeeRepository.countByManagerPersonDocumentNumber(employeeToDelete.getPersonDocumentNumber());
        if (subordinateCount > 0) {
            log.warn("Attempted to delete employee {} who is a manager for {} other employee(s).", code, subordinateCount);
            throw new ReferentialIntegrityException("exception.organization.employee.cannot-delete-is-manager", subordinateCount);
        }

        employeeRepository.delete(employeeToDelete);
        log.info("Successfully deleted employee with code: {}", code);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDetailsDTO getByCode(UUID code) {
        log.info("Fetching details for employee with code: {}", code);
        EmployeeEntity employee = findEmployeeByCode(code);
        EmployeeDetailsDTO dto = employeeMapper.toDetailsDTO(employee);
        
        // Obtener foto de la persona
        String photoUrl = getPersonPhotoUrl(employee.getPerson());
        
        // Crear nuevo DTO con foto
        return new EmployeeDetailsDTO(
                dto.publicId(),
                dto.documentNumber(),
                dto.names(),
                dto.paternalLastname(),
                dto.maternalLastname(),
                dto.dob(),
                dto.subsidiaryName(),
                dto.positionName(),
                dto.areaName(),
                dto.salary(),
                dto.manager(),
                photoUrl
        );
    }
    
    /**
     * Método auxiliar para obtener la URL de la foto de una persona
     */
    private String getPersonPhotoUrl(PersonEntity person) {
        if (person == null) {
            return null;
        }
        try {
            List<ImageEntity> images = imageUseCase.getImagesByImageable(person);
            if (!images.isEmpty()) {
                return images.get(0).getUrl();
            }
        } catch (Exception e) {
            log.warn("Error obteniendo foto para persona {}: {}", person.getDocumentNumber(), e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public CommandEmployeeResponse getCommandResponseByPublicId(UUID publicId) {
        log.info("Fetching command response for employee with public ID: {}", publicId);
        EmployeeEntity employee = findEmployeeByCode(publicId);
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<EmployeeListDTO> findAllPaged(String documentNumber, UUID subsidiaryId, UUID positionId, Boolean isNational, Pageable pageable) {
        log.info("Fetching paged list of employees with filters: documentNumber={}, subsidiaryId={}, positionId={}, isNational={}, page={}, size={}", documentNumber, subsidiaryId, positionId, isNational, pageable.getPageNumber(), pageable.getPageSize());

        UUID documentTypePublicId = null;
        if (isNational != null) {
            if (isNational) {
                documentTypePublicId = documentTypeUseCase.getDniDocumentType().getPublicId();
            } else {
                documentTypePublicId = documentTypeUseCase.getForeignDocumentType().getPublicId();
            }
        }

        // La Specification manejará el ordenamiento internamente para campos especiales
        // Usamos el Pageable original, pero la Specification ignorará el Sort si es necesario
        Specification<EmployeeEntity> spec = EmployeeSpecification.filterBy(documentNumber, null, subsidiaryId, positionId, documentTypePublicId, pageable.getSort());
        Page<EmployeeEntity> employeePage = employeeRepository.findAll(spec, pageable);
        
        // Mapear y agregar fotos
        List<EmployeeListDTO> dtosWithPhotos = employeePage.getContent().stream()
                .map(employee -> {
                    EmployeeListDTO dto = employeeMapper.toListDTO(employee);
                    String photoUrl = getPersonPhotoUrl(employee.getPerson());
                    return new EmployeeListDTO(
                            dto.publicId(),
                            dto.documentNumber(),
                            dto.names(),
                            dto.paternalLastname(),
                            dto.maternalLastname(),
                            dto.subsidiaryName(),
                            dto.positionName(),
                            dto.isNational(),
                            dto.createdAt(),
                            dto.updatedAt(),
                            photoUrl
                    );
                })
                .collect(Collectors.toList());
        
        return new PagedResult<>(
                dtosWithPhotos,
                employeePage.getTotalElements(),
                employeePage.getNumber(),
                employeePage.getTotalPages(),
                employeePage.isFirst(),
                employeePage.isLast(),
                employeePage.hasNext(),
                employeePage.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSelectOptionDTO> getSelectOptions(UUID positionPublicId) {
        log.info("Fetching employee select options for position public ID: {}", positionPublicId);
        if (positionPublicId == null) {
            return Collections.emptyList();
        }

        PositionEntity position = positionService.findByPublicId(positionPublicId);
        PositionEntity requiredManagerPosition = position.getRequiredManagerPosition();

        if (requiredManagerPosition == null) {
            log.warn("Position '{}' requires a manager, but no required manager position is defined.", position.getName());
            throw new BusinessValidationException("exception.organization.employee.manager-position-not-defined", position.getName());
        }

        List<EmployeeEntity> potentialManagers = employeeRepository.findAllByPosition(requiredManagerPosition);
        log.info("Found {} potential managers for position '{}' with required manager position '{}'", potentialManagers.size(), position.getName(), requiredManagerPosition.getName());
        return employeeMapper.toSelectOptionDTOs(potentialManagers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationalChartNodeDTO> getOrganizationalChart(UUID subsidiaryId, Integer levels) {
        // levels = 0 means show all levels, levels > 0 means show only that many levels
        int maxLevels = (levels == null || levels <= 0) ? Integer.MAX_VALUE : levels;

        if (subsidiaryId == null) {
            log.info("Building complete organizational chart from CEO with {} levels", maxLevels == Integer.MAX_VALUE ? "all" : maxLevels);
            return buildCompleteOrganizationalChart(maxLevels);
        } else {
            log.info("Building organizational chart for subsidiary ID: {} with {} levels", subsidiaryId, maxLevels == Integer.MAX_VALUE ? "all" : maxLevels);
            return buildSubsidiaryOrganizationalChart(subsidiaryId, maxLevels);
        }
    }

    private List<OrganizationalChartNodeDTO> buildCompleteOrganizationalChart(int maxLevels) {
        // Get all active employees
        List<EmployeeEntity> allEmployees = employeeRepository.findAllActive();

        // Validate that we have exactly one CEO
        long ceoCount = allEmployees.stream().filter(e -> e.getManager() == null).count();
        if (ceoCount == 0) {
            log.warn("No CEO found in organizational structure");
            return new ArrayList<>();
        }
        if (ceoCount > 1) {
            log.error("Multiple CEOs found in organizational structure: {}", ceoCount);
            throw new BusinessValidationException("exception.organization.multiple-ceos-found");
        }

        Map<UUID, OrganizationalChartNodeDTO> nodeMap = createNodeMap(allEmployees);

        List<OrganizationalChartNodeDTO> rootNodes = new ArrayList<>();

        allEmployees.forEach(employee -> {
            OrganizationalChartNodeDTO node = nodeMap.get(employee.getCode());
            if (employee.getManager() == null) {
                // This is a CEO (no manager)
                rootNodes.add(node);
            } else {
                OrganizationalChartNodeDTO managerNode = nodeMap.get(employee.getManager().getCode());
                if (managerNode != null) {
                    managerNode.children().add(node);
                }
            }
        });

        // Trim tree to specified levels
        rootNodes.forEach(root -> trimTreeToMaxLevels(root, maxLevels, 1));

        log.info("Successfully built complete organizational chart with {} root node(s)", rootNodes.size());
        return rootNodes;
    }

    private List<OrganizationalChartNodeDTO> buildSubsidiaryOrganizationalChart(UUID subsidiaryId, int maxLevels) {
        // Strategy: Find the subsidiary head and build the tree from there
        List<EmployeeEntity> subsidiaryEmployees = employeeRepository.findAllBySubsidiary_PublicId(subsidiaryId);

        if (subsidiaryEmployees.isEmpty()) {
            log.warn("No employees found for subsidiary ID: {}", subsidiaryId);
            return new ArrayList<>();
        }

        // Find the highest-level employee in this subsidiary (the one whose manager is outside this subsidiary or is CEO)
        EmployeeEntity subsidiaryHead = findSubsidiaryHead(subsidiaryEmployees, subsidiaryId);

        // Build tree starting from subsidiary head
        Map<UUID, OrganizationalChartNodeDTO> nodeMap = createNodeMap(subsidiaryEmployees);
        List<OrganizationalChartNodeDTO> rootNodes = new ArrayList<>();

        if (subsidiaryHead != null) {
            OrganizationalChartNodeDTO rootNode = nodeMap.get(subsidiaryHead.getCode());
            if (rootNode != null) {
                rootNodes.add(rootNode);
                buildSubsidiaryHierarchy(subsidiaryEmployees, nodeMap, subsidiaryHead);

                // Trim tree to specified levels
                trimTreeToMaxLevels(rootNode, maxLevels, 1);
            }
        }

        log.info("Successfully built subsidiary organizational chart with {} root node(s) for subsidiary ID: {}", rootNodes.size(), subsidiaryId);
        return rootNodes;
    }

    private EmployeeEntity findSubsidiaryHead(List<EmployeeEntity> subsidiaryEmployees, UUID subsidiaryId) {
        // Find employee within this subsidiary whose manager is OUTSIDE this subsidiary (or is CEO without manager)
        return subsidiaryEmployees.stream()
                .filter(emp -> {
                    if (emp.getManager() == null) {
                        // This employee has no manager (is CEO), and belongs to this subsidiary
                        return true;
                    }
                    // Check if this employee's manager belongs to a DIFFERENT subsidiary
                    return emp.getManager().getSubsidiary() != null &&
                           !emp.getManager().getSubsidiary().getPublicId().equals(subsidiaryId);
                })
                .findFirst()
                .orElse(null);
    }

    private void buildSubsidiaryHierarchy(List<EmployeeEntity> employees, Map<UUID, OrganizationalChartNodeDTO> nodeMap, EmployeeEntity head) {
        employees.forEach(employee -> {
            if (!employee.getCode().equals(head.getCode()) && employee.getManager() != null) {
                OrganizationalChartNodeDTO node = nodeMap.get(employee.getCode());
                // Only link to manager if the manager is also within this subsidiary (in our nodeMap)
                OrganizationalChartNodeDTO managerNode = nodeMap.get(employee.getManager().getCode());
                if (node != null && managerNode != null) {
                    managerNode.children().add(node);
                }
                // If managerNode is null, it means the manager is outside this subsidiary,
                // so this employee becomes a root node (which should be the subsidiary head)
            }
        });
    }

    private Map<UUID, OrganizationalChartNodeDTO> createNodeMap(List<EmployeeEntity> employees) {
        return employees.stream()
                .collect(Collectors.toMap(
                        EmployeeEntity::getCode,
                        e -> new OrganizationalChartNodeDTO(
                                true, // expanded by default
                                new OrganizationalChartNodeDTO.EmployeeDataDTO(
                                        e.getCode(),
                                        generateAvatarUrl(e.getPerson()), // generate avatar URL
                                        e.getPerson().getNames(),
                                        e.getPosition().getName()
                                ),
                                new ArrayList<>() // empty children list
                        )
                ));
    }

    private void trimTreeToMaxLevels(OrganizationalChartNodeDTO node, int maxLevels, int currentLevel) {
        if (currentLevel >= maxLevels) {
            // We've reached the maximum level, clear all children
            node.children().clear();
            return;
        }

        // If there are no children, this branch naturally ends here - that's fine
        if (node.children().isEmpty()) {
            return;
        }

        // Create a copy of children list to avoid concurrent modification
        List<OrganizationalChartNodeDTO> children = new ArrayList<>(node.children());

        // Recursively trim each child
        for (OrganizationalChartNodeDTO child : children) {
            trimTreeToMaxLevels(child, maxLevels, currentLevel + 1);
        }

        // Note: We don't remove children that naturally have fewer levels
        // The trimming only happens when we exceed maxLevels
    }

    private String generateAvatarUrl(PersonEntity person) {
        // Generate avatar URL based on person's initials or use a default
        String initials = getInitials(person.getNames());
        return String.format("https://ui-avatars.com/api/?name=%s&background=random&color=fff&size=128", initials);
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "NN";
        }
        String[] names = fullName.trim().split("\\s+");
        if (names.length == 1) {
            return names[0].substring(0, Math.min(2, names[0].length())).toUpperCase();
        }
        return (names[0].substring(0, 1) + names[names.length - 1].substring(0, 1)).toUpperCase();
    }

    @Override
    public List<StateSelectOptionDTO> getStatesSelectOptions() {
        return stateService.findStateOptionsByDomainName(EmployeeEntity.TABLE_NAME);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeEntity> findByPersonDocumentNumber(String documentNumber) {
        log.debug("Searching for employee with document number: {}", documentNumber);
        Optional<EmployeeEntity> employee = employeeRepository.findByPersonDocumentNumber(documentNumber);
        log.debug("Employee {} for document number: {}", employee.isPresent() ? "found" : "not found", documentNumber);
        return employee;
    }

    @Override
    public EmployeeEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find employee entity with public ID: {}", publicId);
        return employeeRepository.findByCode(publicId)
                .orElseThrow(() -> {
                    log.warn("Employee with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.organization.employee.not-found", publicId);
                });
    }

    @Override
    public EmployeeEntity findByDocumentNumber(String documentNumber) {
        log.debug("Attempting to find employee entity with document number: {}", documentNumber);
        return employeeRepository.findByPersonDocumentNumber(documentNumber)
                .orElseThrow(() -> {
                    log.warn("Employee with document number {} not found.", documentNumber);
                    return new IdentifierNotFoundException("exception.organization.employee.not-found-by-document", documentNumber);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeSearchResponse searchByDocumentNumber(String documentNumber) {
        log.info("Searching employee by document number: {}", documentNumber);
        EmployeeEntity employee = findByDocumentNumber(documentNumber);

        // No validamos el estado del empleado, ya que el filtro normal tampoco lo hace
        // Esto permite buscar empleados en cualquier estado para asignar responsables de firma

        String fullName = employee.getPerson() != null ? employee.getPerson().getNames() : null;
        String positionName = employee.getPosition() != null ? employee.getPosition().getName() : null;
        UUID subsidiaryId = employee.getSubsidiary() != null ? employee.getSubsidiary().getPublicId() : null;
        String subsidiaryName = employee.getSubsidiary() != null ? employee.getSubsidiary().getName() : null;

        return new EmployeeSearchResponse(
                employee.getPersonDocumentNumber(),
                fullName,
                positionName,
                subsidiaryId,
                subsidiaryName
        );
    }

    private EmployeeEntity findEmployeeByCode(UUID code) {
        return findByPublicId(code);
    }

    private void handlePositionChange(EmployeeEntity employee, PositionEntity newPosition, UUID managerCode) {
        boolean isPositionChanging = employee.getPosition() == null || !Objects.equals(employee.getPosition().getId(), newPosition.getId());

        if (isPositionChanging && newPosition.isUnique() && employeeRepository.existsByPositionId(newPosition.getId())) {
            log.warn("Attempted to move employee {} to a unique and already filled position '{}'.", employee.getCode(), newPosition.getName());
            throw new PositionAlreadyFilledException("exception.organization.position.already-filled", newPosition.getName());
        }

        employee.setPosition(newPosition);

        if (managerCode != null) {
            if (managerCode.equals(employee.getCode())) {
                throw new BusinessValidationException("exception.organization.employee.self-management");
            }
            EmployeeEntity manager = findEmployeeByCode(managerCode);
            employee.setManager(manager);
        } else {
            employee.setManager(null);
        }
    }

    @Override
    @Transactional
    public void updateCustomSalary(String documentNumber, java.math.BigDecimal newSalary) {
        log.info("Updating custom salary for employee with document number: {}", documentNumber);

        EmployeeEntity employee = employeeRepository.findById(documentNumber)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.employee.not-found", documentNumber));

        employee.setCustomSalary(newSalary);
        employeeRepository.save(employee);

        log.info("Successfully updated custom salary for employee: {}", documentNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeMeResponse getMyInfo(String username) {
        log.info("Fetching employee information for username: {}", username);

        EmployeeEntity employee = employeeRepository.findById(username)
                .orElseThrow(() -> {
                    log.warn("Employee with document number {} not found", username);
                    return new IdentifierNotFoundException("exception.organization.employee.not-found", username);
                });

        PersonEntity person = employee.getPerson();
        PositionEntity position = employee.getPosition();
        SubsidiaryEntity subsidiary = employee.getSubsidiary();

        // Obtener foto de la persona
        String photoUrl = null;
        if (person != null) {
            try {
                List<ImageEntity> images = imageUseCase.getImagesByImageable(person);
                if (!images.isEmpty()) {
                    photoUrl = images.get(0).getUrl();
                }
            } catch (Exception e) {
                log.warn("Error obteniendo foto para persona {}: {}", person.getDocumentNumber(), e.getMessage());
            }
        }

        return new EmployeeMeResponse(
                employee.getCode(),
                employee.getPersonDocumentNumber(),
                person != null ? person.getNames() : null,
                person != null ? person.getPaternalLastname() : null,
                person != null ? person.getMaternalLastname() : null,
                person != null ? person.getDob() : null,
                person != null ? person.getGender() : null,
                position != null ? position.getName() : null,
                subsidiary != null ? subsidiary.getPublicId() : null,
                subsidiary != null ? subsidiary.getName() : null,
                photoUrl
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeCacheResponse getEmployeeCacheByDocumentNumber(String documentNumber) {
        log.info("Fetching employee cache data for document number: {}", documentNumber);

        // Buscar empleado por número de documento
        EmployeeEntity employee = findByDocumentNumber(documentNumber);

        // Validar que el empleado esté en un estado válido (ACTIVO o CREADO)
        // Permitimos CREADO porque un empleado puede tener contrato firmado pero aún no estar activado
        if (employee.getState() == null) {
            log.warn("Employee with document number {} has no state", documentNumber);
            throw new IdentifierNotFoundException("exception.organization.employee.not-active", documentNumber);
        }
        
        String stateCode = employee.getState().getCode();
        boolean isValidState = EmployeeStateEnum.ACTIVO.getCode().equals(stateCode) || 
                               EmployeeStateEnum.CREADO.getCode().equals(stateCode);
        
        if (!isValidState) {
            log.warn("Employee with document number {} is in invalid state: {} (expected ACTIVO or CREADO)", 
                    documentNumber, stateCode);
            throw new IdentifierNotFoundException("exception.organization.employee.not-active", documentNumber);
        }

        // Validar que no esté soft-deleted (deletedAt debe ser null)
        if (employee.getDeletedAt() != null) {
            log.warn("Employee with document number {} is soft-deleted", documentNumber);
            throw new IdentifierNotFoundException("exception.organization.employee.deleted", documentNumber);
        }

        PersonEntity person = employee.getPerson();
        SubsidiaryEntity subsidiary = employee.getSubsidiary();
        PositionEntity position = employee.getPosition();

        // Validar que tenga persona asociada
        if (person == null) {
            log.error("Employee {} does not have an associated person", documentNumber);
            throw new BusinessValidationException("exception.organization.employee.no-person", documentNumber);
        }

        // Validar que tenga subsidiaria asociada
        if (subsidiary == null) {
            log.error("Employee {} does not have an associated subsidiary", documentNumber);
            throw new BusinessValidationException("exception.organization.employee.no-subsidiary", documentNumber);
        }

        // Validar que tenga posición asociada
        if (position == null) {
            log.error("Employee {} does not have an associated position", documentNumber);
            throw new BusinessValidationException("exception.organization.employee.no-position", documentNumber);
        }

        return new EmployeeCacheResponse(
                employee.getPersonDocumentNumber(),
                person.getNames(),
                person.getPaternalLastname(),
                person.getMaternalLastname(),
                subsidiary.getPublicId(),
                position.getPublicId()
        );
    }

    private boolean isUpdateRedundant(UpdateEmployeeRequest request, EmployeeEntity entity) {
        return Objects.equals(request.subsidiaryPublicId(), entity.getSubsidiary().getPublicId()) &&
               Objects.equals(request.positionPublicId(), entity.getPosition().getPublicId()) &&
               Objects.equals(request.managerCode(), (entity.getManager() != null ? entity.getManager().getCode() : null));
    }
}
