package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.hiring.mapper.IVariableMapper;
import com.agropay.core.hiring.model.variable.CommandVariableResponse;
import com.agropay.core.hiring.model.variable.CreateVariableRequest;
import com.agropay.core.hiring.model.variable.UpdateVariableRequest;
import com.agropay.core.hiring.model.variable.VariableListDTO;
import com.agropay.core.hiring.model.variable.VariableSelectOptionDTO;
import com.agropay.core.hiring.model.variable.AssociateMethodsRequest;
import com.agropay.core.hiring.model.variable.VariableWithValidationDTO;
import com.agropay.core.hiring.model.variable.VariableMethodRequest;
import com.agropay.core.hiring.model.variable.AssociatedMethodDTO;
import com.agropay.core.hiring.model.variable.CreateVariableWithValidationRequest;
import com.agropay.core.hiring.model.variable.UpdateVariableWithValidationRequest;
import com.agropay.core.hiring.model.variable.VariableValidationRequest;
import com.agropay.core.validation.domain.DynamicVariableEntity;
import com.agropay.core.validation.domain.DynamicVariableMethodEntity;
import com.agropay.core.validation.domain.ValidationMethodEntity;
import com.agropay.core.validation.persistence.IDynamicVariableRepository;
import com.agropay.core.validation.persistence.IDynamicVariableMethodRepository;
import com.agropay.core.validation.persistence.IValidationMethodRepository;
import com.agropay.core.validation.application.service.VariableValidationService;
import com.agropay.core.hiring.persistence.IVariableRepository;
import com.agropay.core.hiring.persistence.VariableSpecification;
import com.agropay.core.hiring.application.usecase.IVariableUseCase;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.NoChangesDetectedException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VariableServiceImpl implements IVariableUseCase {

    private final IVariableRepository variableRepository;
    private final IVariableMapper variableMapper;
    private final IDynamicVariableRepository dynamicVariableRepository;
    private final IDynamicVariableMethodRepository dynamicVariableMethodRepository;
    private final IValidationMethodRepository validationMethodRepository;
    private final VariableValidationService validationService;

    @Override
    @Transactional
    public CommandVariableResponse create(CreateVariableRequest request) {
        log.info("Attempting to create variable with code: {}", request.code());

        String formattedCode = formatCode(request.code());
        String trimmedName = request.name().trim();

        if (variableRepository.existsByCode(formattedCode)) {
            throw new UniqueValidationException("exception.hiring.variable.code-unique", formattedCode);
        }

        if (variableRepository.existsByName(trimmedName)) {
            throw new UniqueValidationException("exception.hiring.variable.name-unique", trimmedName);
        }

        VariableEntity variable = VariableEntity.builder()
                .publicId(UUID.randomUUID())
                .code(formattedCode)
                .name(trimmedName)
                .defaultValue(request.defaultValue() != null ? request.defaultValue().trim() : null)
                .build();

        VariableEntity savedVariable = variableRepository.save(variable);
        log.info("Variable created successfully: code={}, publicId={}", savedVariable.getCode(), savedVariable.getPublicId());

        return variableMapper.toCommandResponse(savedVariable);
    }

    @Override
    @Transactional
    public CommandVariableResponse update(UUID publicId, UpdateVariableRequest request) {
        log.info("Attempting to update variable with publicId: {}", publicId);

        VariableEntity variable = findByPublicId(publicId);
        String formattedCode = formatCode(request.code());
        String trimmedName = request.name().trim();

        if (isUpdateRedundant(request, variable, formattedCode)) {
            throw new NoChangesDetectedException("exception.shared.no-changes-detected");
        }

        if (!variable.getCode().equals(formattedCode) && variableRepository.existsByCode(formattedCode)) {
            throw new UniqueValidationException("exception.hiring.variable.code-unique", formattedCode);
        }

        if (!variable.getName().equals(trimmedName) && variableRepository.existsByName(trimmedName)) {
            throw new UniqueValidationException("exception.hiring.variable.name-unique", trimmedName);
        }

        variable.setCode(formattedCode);
        variable.setName(trimmedName);
        variable.setDefaultValue(request.defaultValue() != null ? request.defaultValue().trim() : null);

        VariableEntity updatedVariable = variableRepository.save(variable);
        log.info("Variable updated successfully: code={}, publicId={}", updatedVariable.getCode(), publicId);

        return variableMapper.toCommandResponse(updatedVariable);
    }

    @Override
    @Transactional
    public void deleteByPublicId(UUID publicId) {
        log.info("Attempting to delete variable with publicId: {}", publicId);

        VariableEntity variable = findByPublicId(publicId);

        try {
            variableRepository.delete(variable);
            log.info("Variable deleted successfully: publicId={}", publicId);
        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete variable due to referential integrity: publicId={}", publicId);
            throw new com.agropay.core.shared.exceptions.ReferentialIntegrityException(
                "exception.hiring.variable.referential-integrity", publicId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableSelectOptionDTO> getSelectOptions() {
        log.info("Fetching select options for variables");
        return variableMapper.toSelectOptionDTOs(variableRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableSelectOptionDTO> getSelectOptions(String name) {
        log.info("Fetching select options for variables with name filter: '{}'", name);

        Specification<VariableEntity> spec = VariableSpecification.filterByName(name);
        List<VariableEntity> variables = variableRepository.findAll(spec);

        return variableMapper.toSelectOptionDTOs(variables);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<VariableListDTO> findAllPaged(String code, String name, Pageable pageable) {
        log.info("Fetching paged list of variables with filters: code='{}', name='{}', page={}, size={}",
                code, name, pageable.getPageNumber(), pageable.getPageSize());

        Specification<VariableEntity> spec = VariableSpecification.filterBy(code, name);
        Page<VariableEntity> variablePage = variableRepository.findAll(spec, pageable);

        List<Short> variableIds = variablePage.getContent().stream()
                .map(VariableEntity::getId)
                .toList();

        Map<Short, Long> methodCounts = getMethodCountsMap(variableIds);

        List<VariableListDTO> dtoList = variablePage.getContent().stream()
                .map(variable -> variableMapper.toListDTOWithCount(variable, methodCounts.get(variable.getId())))
                .toList();

        return new PagedResult<>(
                dtoList,
                variablePage.getTotalElements(),
                variablePage.getNumber(),
                variablePage.getTotalPages(),
                variablePage.isFirst(),
                variablePage.isLast(),
                variablePage.hasNext(),
                variablePage.hasPrevious()
        );
    }

    private Map<Short, Long> getMethodCountsMap(List<Short> variableIds) {
        if (variableIds.isEmpty()) {
            return Map.of();
        }

        List<Map<String, Object>> countResults = dynamicVariableMethodRepository.countMethodsByVariableIds(variableIds);
        return countResults.stream()
                .collect(Collectors.toMap(
                        result -> (Short) result.get("variableId"),
                        result -> ((Number) result.get("methodCount")).longValue()
                ));
    }


    public VariableEntity findByPublicId(UUID publicId) {
        return variableRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.variable.not-found", publicId));
    }

    private String formatCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim()
                .replaceAll("\\s+", "_")
                .toUpperCase();
    }

    private boolean isUpdateRedundant(UpdateVariableRequest request, VariableEntity variable, String formattedCode) {
        String currentDefaultValue = variable.getDefaultValue();
        String newDefaultValue = request.defaultValue() != null ? request.defaultValue().trim() : null;

        return formattedCode.equals(variable.getCode()) &&
               request.name().trim().equals(variable.getName()) &&
               java.util.Objects.equals(newDefaultValue, currentDefaultValue);
    }

    @Override
    @Transactional
    public CommandVariableResponse associateMethods(UUID variablePublicId, AssociateMethodsRequest request) {
        log.info("Associating methods to variable with publicId: {}", variablePublicId);

        VariableEntity variable = findByPublicId(variablePublicId);

        if (variable.getDynamicVariable() != null) {
            throw new IllegalStateException("Variable ya tiene métodos asociados. Use updateMethods para actualizar.");
        }

        List<UUID> methodIds = request.methods().stream().map(VariableMethodRequest::methodPublicId).toList();
        List<VariableValidationService.MethodValidationRequest> adaptedMethods = request.methods().stream()
                .map(method -> (VariableValidationService.MethodValidationRequest) new VariableMethodAdapter(method)).toList();

        validationService.validateMethodsExist(methodIds);
        validationService.validateRequiredValues(adaptedMethods);
        validationService.validateExecutionOrder(adaptedMethods);

        DynamicVariableEntity dynamicVariable = createDynamicVariableEntity(variable, request);
        dynamicVariable = dynamicVariableRepository.save(dynamicVariable);

        List<DynamicVariableMethodEntity> variableMethods = createVariableMethods(dynamicVariable, request.methods());
        dynamicVariableMethodRepository.saveAll(variableMethods);

        String finalRegex = validationService.generateFinalRegex(variableMethods);
        dynamicVariable.setFinalRegex(finalRegex);
        dynamicVariable = dynamicVariableRepository.save(dynamicVariable);

        variable.setDynamicVariable(dynamicVariable);
        variable = variableRepository.save(variable);

        log.info("Successfully associated methods to variable: {}", variablePublicId);
        return variableMapper.toCommandResponse(variable);
    }

    @Override
    @Transactional
    public CommandVariableResponse updateMethods(UUID variablePublicId, AssociateMethodsRequest request) {
        log.info("Updating methods for variable with publicId: {}", variablePublicId);

        VariableEntity variable = findByPublicId(variablePublicId);
        DynamicVariableEntity dynamicVariable = variable.getDynamicVariable();

        if (dynamicVariable == null) {
            throw new IllegalStateException("Variable no tiene métodos asociados. Use associateMethods para asociar.");
        }

        List<UUID> methodIds = request.methods().stream().map(VariableMethodRequest::methodPublicId).toList();
        List<VariableValidationService.MethodValidationRequest> adaptedMethods = request.methods().stream()
                .map(method -> (VariableValidationService.MethodValidationRequest) new VariableMethodAdapter(method)).toList();

        validationService.validateMethodsExist(methodIds);
        validationService.validateRequiredValues(adaptedMethods);
        validationService.validateExecutionOrder(adaptedMethods);

        dynamicVariable.setErrorMessage(request.errorMessage());

        List<DynamicVariableMethodEntity> existingMethods = dynamicVariableMethodRepository
                .findByDynamicVariableOrderByExecutionOrder(dynamicVariable);
        dynamicVariableMethodRepository.softDeleteAll(existingMethods, "SYSTEM");

        List<DynamicVariableMethodEntity> newVariableMethods = createVariableMethods(dynamicVariable, request.methods());
        dynamicVariableMethodRepository.saveAll(newVariableMethods);

        String finalRegex = validationService.generateFinalRegex(newVariableMethods);
        dynamicVariable.setFinalRegex(finalRegex);
        dynamicVariableRepository.save(dynamicVariable);

        log.info("Successfully updated methods for variable: {}", variablePublicId);
        return variableMapper.toCommandResponse(variable);
    }

    @Override
    @Transactional
    public void disassociateMethods(UUID variablePublicId) {
        log.info("Disassociating methods from variable with publicId: {}", variablePublicId);

        VariableEntity variable = findByPublicId(variablePublicId);
        DynamicVariableEntity dynamicVariable = variable.getDynamicVariable();

        if (dynamicVariable == null) {
            log.warn("Variable {} already has no methods associated", variablePublicId);
            return;
        }

        List<DynamicVariableMethodEntity> existingMethods = dynamicVariableMethodRepository
                .findByDynamicVariableOrderByExecutionOrder(dynamicVariable);
        dynamicVariableMethodRepository.softDeleteAll(existingMethods, "SYSTEM");

        dynamicVariableRepository.delete(dynamicVariable);

        variable.setDynamicVariable(null);
        variableRepository.save(variable);

        log.info("Successfully disassociated methods from variable: {}", variablePublicId);
    }

    @Override
    @Transactional(readOnly = true)
    public VariableWithValidationDTO getVariableWithValidation(UUID variablePublicId) {
        log.info("Getting variable with validation details: {}", variablePublicId);

        VariableEntity variable = findByPublicId(variablePublicId);
        VariableWithValidationDTO baseDto = variableMapper.toVariableWithValidationDTO(variable);

        List<AssociatedMethodDTO> methods = List.of();
        if (variable.getDynamicVariable() != null) {
            List<DynamicVariableMethodEntity> methodEntities = dynamicVariableMethodRepository
                    .findByDynamicVariableOrderByExecutionOrder(variable.getDynamicVariable());

            methods = methodEntities.stream()
                    .map(method -> new AssociatedMethodDTO(
                            method.getValidationMethod().getPublicId(),
                            method.getValidationMethod().getName(),
                            method.getValidationMethod().getDescription(),
                            method.getValidationMethod().getRequiresValue(),
                            method.getValidationMethod().getMethodType().name(),
                            method.getValue(),
                            method.getExecutionOrder()
                    ))
                    .toList();
        }

        return new VariableWithValidationDTO(
                baseDto.publicId(),
                baseDto.code(),
                baseDto.name(),
                baseDto.defaultValue(),
                baseDto.finalRegex(),
                baseDto.errorMessage(),
                methods
        );
    }

    @Override
    @Transactional
    public CommandVariableResponse createWithValidation(CreateVariableWithValidationRequest request) {
        log.info("Creating variable with validation: code={}", request.code());

        String formattedCode = formatCode(request.code());
        String trimmedName = request.name().trim();

        if (variableRepository.existsByCode(formattedCode)) {
            throw new UniqueValidationException("exception.hiring.variable.code-unique", formattedCode);
        }

        if (variableRepository.existsByName(trimmedName)) {
            throw new UniqueValidationException("exception.hiring.variable.name-unique", trimmedName);
        }

        VariableEntity variable = VariableEntity.builder()
                .publicId(UUID.randomUUID())
                .code(formattedCode)
                .name(trimmedName)
                .defaultValue(request.defaultValue() != null ? request.defaultValue().trim() : null)
                .build();

        VariableEntity savedVariable = variableRepository.save(variable);
        log.info("Variable created successfully: code={}, publicId={}", savedVariable.getCode(), savedVariable.getPublicId());

        if (request.validation() != null && request.validation().methods() != null && !request.validation().methods().isEmpty()) {
            log.info("Processing validation methods for variable: {}", savedVariable.getPublicId());
            processValidationMethods(savedVariable, request.validation());
        }

        return variableMapper.toCommandResponse(savedVariable);
    }

    @Override
    @Transactional
    public CommandVariableResponse updateWithValidation(UUID publicId, UpdateVariableWithValidationRequest request) {
        log.info("Updating variable with validation: publicId={}", publicId);

        VariableEntity variable = findByPublicId(publicId);
        String formattedCode = formatCode(request.code());
        String trimmedName = request.name().trim();

        if (isUpdateRedundant(request, variable, formattedCode)) {
            throw new NoChangesDetectedException("exception.shared.no-changes-detected");
        }

        if (!variable.getCode().equals(formattedCode) && variableRepository.existsByCode(formattedCode)) {
            throw new UniqueValidationException("exception.hiring.variable.code-unique", formattedCode);
        }

        if (!variable.getName().equals(trimmedName) && variableRepository.existsByName(trimmedName)) {
            throw new UniqueValidationException("exception.hiring.variable.name-unique", trimmedName);
        }

        variable.setCode(formattedCode);
        variable.setName(trimmedName);
        variable.setDefaultValue(request.defaultValue() != null ? request.defaultValue().trim() : null);

        VariableEntity updatedVariable = variableRepository.save(variable);
        log.info("Variable updated successfully: code={}, publicId={}", updatedVariable.getCode(), publicId);

        if (request.validation() != null) {
            if (request.validation().methods() == null || request.validation().methods().isEmpty()) {
                log.info("Disassociating validation methods from variable: {}", publicId);
                disassociateValidationMethods(updatedVariable);
            } else {
                log.info("Processing validation methods for variable: {}", publicId);
                processValidationMethods(updatedVariable, request.validation());
            }
        }

        return variableMapper.toCommandResponse(updatedVariable);
    }

    private void processValidationMethods(VariableEntity variable, VariableValidationRequest validationRequest) {
        List<UUID> methodIds = validationRequest.methods().stream()
                .map(VariableMethodRequest::methodPublicId)
                .toList();

        List<VariableValidationService.MethodValidationRequest> adaptedMethods = validationRequest.methods().stream()
                .map(method -> (VariableValidationService.MethodValidationRequest) new VariableMethodAdapter(method))
                .toList();

        validationService.validateMethodsExist(methodIds);
        validationService.validateRequiredValues(adaptedMethods);
        validationService.validateExecutionOrder(adaptedMethods);

        DynamicVariableEntity dynamicVariable = variable.getDynamicVariable();
        boolean isUpdate = dynamicVariable != null;

        if (isUpdate) {
            log.debug("Updating existing validation methods for variable: {}", variable.getPublicId());
            dynamicVariable.setErrorMessage(validationRequest.errorMessage());

            List<DynamicVariableMethodEntity> existingMethods = dynamicVariableMethodRepository
                    .findByDynamicVariableOrderByExecutionOrder(dynamicVariable);
            dynamicVariableMethodRepository.softDeleteAll(existingMethods, "SYSTEM");
        } else {
            log.debug("Creating new validation methods for variable: {}", variable.getPublicId());
            dynamicVariable = DynamicVariableEntity.builder()
                    .publicId(UUID.randomUUID())
                    .code(variable.getCode() + "_VALIDATION")
                    .name(variable.getName() + " - Validación")
                    .errorMessage(validationRequest.errorMessage())
                    .isActive(true)
                    .build();
            dynamicVariable = dynamicVariableRepository.save(dynamicVariable);
        }

        List<DynamicVariableMethodEntity> variableMethods = createVariableMethods(dynamicVariable, validationRequest.methods());
        dynamicVariableMethodRepository.saveAll(variableMethods);

        String finalRegex = validationService.generateFinalRegex(variableMethods);
        dynamicVariable.setFinalRegex(finalRegex);
        dynamicVariableRepository.save(dynamicVariable);

        if (!isUpdate) {
            variable.setDynamicVariable(dynamicVariable);
            variableRepository.save(variable);
        }
    }

    private void disassociateValidationMethods(VariableEntity variable) {
        DynamicVariableEntity dynamicVariable = variable.getDynamicVariable();

        if (dynamicVariable != null) {
            List<DynamicVariableMethodEntity> existingMethods = dynamicVariableMethodRepository
                    .findByDynamicVariableOrderByExecutionOrder(dynamicVariable);
            dynamicVariableMethodRepository.softDeleteAll(existingMethods, "SYSTEM");

            dynamicVariableRepository.delete(dynamicVariable);

            variable.setDynamicVariable(null);
            variableRepository.save(variable);
        }
    }

    private boolean isUpdateRedundant(UpdateVariableWithValidationRequest request, VariableEntity variable, String formattedCode) {
        String currentDefaultValue = variable.getDefaultValue();
        String newDefaultValue = request.defaultValue() != null ? request.defaultValue().trim() : null;

        boolean basicDataUnchanged = formattedCode.equals(variable.getCode()) &&
                request.name().trim().equals(variable.getName()) &&
                java.util.Objects.equals(newDefaultValue, currentDefaultValue);

        if (!basicDataUnchanged) {
            return false;
        }

        return isValidationUnchanged(request.validation(), variable);
    }

    private boolean isValidationUnchanged(VariableValidationRequest newValidation, VariableEntity variable) {
        DynamicVariableEntity currentDynamicVariable = variable.getDynamicVariable();

        log.debug("Checking if validation is unchanged for variable: {}", variable.getPublicId());
        log.debug("New validation is null: {}, Current dynamic variable is null: {}",
                newValidation == null, currentDynamicVariable == null);

        // Caso 1: Ambos sin validación
        if (newValidation == null && currentDynamicVariable == null) {
            log.debug("Both validations are null - no changes");
            return true;
        }

        // Caso 2: Uno tiene validación y el otro no
        if ((newValidation == null) != (currentDynamicVariable == null)) {
            log.debug("One has validation and the other doesn't - validation changed");
            return false;
        }

        // Caso 3: Ambos tienen validación - comparar contenido
        if (newValidation != null && currentDynamicVariable != null) {
            log.debug("Both have validation - comparing content");
            boolean isEqual = isValidationContentEqual(newValidation, currentDynamicVariable);
            log.debug("Validation content comparison result: {}", isEqual);
            return isEqual;
        }

        return true;
    }

    private boolean isValidationContentEqual(VariableValidationRequest newValidation, DynamicVariableEntity currentDynamicVariable) {
        log.debug("Comparing validation content for dynamic variable: {}", currentDynamicVariable.getPublicId());

        String currentErrorMessage = currentDynamicVariable.getErrorMessage();
        String newErrorMessage = newValidation.errorMessage();
        log.debug("Comparing error messages - current: '{}', new: '{}'", currentErrorMessage, newErrorMessage);
        if (!java.util.Objects.equals(currentErrorMessage, newErrorMessage)) {
            log.debug("Error messages are different, validation has changed");
            return false;
        }

        // Obtener métodos actuales
        List<DynamicVariableMethodEntity> currentMethods = dynamicVariableMethodRepository
                .findByDynamicVariableOrderByExecutionOrder(currentDynamicVariable);

        List<VariableMethodRequest> newMethods = newValidation.methods() != null ? newValidation.methods() : List.of();

        log.debug("Comparing method counts - current: {}, new: {}", currentMethods.size(), newMethods.size());
        // Comparar cantidad de métodos
        if (currentMethods.size() != newMethods.size()) {
            log.debug("Method counts are different, validation has changed");
            return false;
        }

        // Comparar cada método (asumiendo que están ordenados por executionOrder)
        for (int i = 0; i < currentMethods.size(); i++) {
            DynamicVariableMethodEntity currentMethod = currentMethods.get(i);
            VariableMethodRequest newMethod = newMethods.get(i);

            log.debug("Comparing method {} - current: [methodId={}, value='{}', order={}], new: [methodId={}, value='{}', order={}]",
                    i,
                    currentMethod.getValidationMethod().getPublicId(),
                    currentMethod.getValue(),
                    currentMethod.getExecutionOrder(),
                    newMethod.methodPublicId(),
                    newMethod.value(),
                    newMethod.executionOrder());

            if (!currentMethod.getValidationMethod().getPublicId().equals(newMethod.methodPublicId()) ||
                !java.util.Objects.equals(currentMethod.getValue(), newMethod.value()) ||
                !currentMethod.getExecutionOrder().equals(newMethod.executionOrder())) {
                log.debug("Method {} is different, validation has changed", i);
                return false;
            }
        }

        log.debug("All validation content is identical");
        return true;
    }

    private DynamicVariableEntity createDynamicVariableEntity(VariableEntity variable, AssociateMethodsRequest request) {
        return DynamicVariableEntity.builder()
                .publicId(UUID.randomUUID())
                .code(variable.getCode() + "_VALIDATION")
                .name(variable.getName() + " - Validación")
                .errorMessage(request.errorMessage())
                .isActive(true)
                .build();
    }

    private List<DynamicVariableMethodEntity> createVariableMethods(DynamicVariableEntity dynamicVariable, List<VariableMethodRequest> methodRequests) {
        return methodRequests.stream()
                .map(methodRequest -> {
                    ValidationMethodEntity method = validationMethodRepository.findByPublicId(methodRequest.methodPublicId())
                            .orElseThrow(() -> new IdentifierNotFoundException("exception.validation.method.not-found", methodRequest.methodPublicId().toString()));

                    return DynamicVariableMethodEntity.builder()
                            .dynamicVariable(dynamicVariable)
                            .validationMethod(method)
                            .value(methodRequest.value())
                            .executionOrder(methodRequest.executionOrder())
                            .build();
                })
                .toList();
    }

}
