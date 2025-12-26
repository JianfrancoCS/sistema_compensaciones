package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.application.usecase.IContractTemplateUseCase;
import com.agropay.core.hiring.application.usecase.IContractTypeUseCase;
import com.agropay.core.hiring.domain.ContractTemplateEntity;
import com.agropay.core.hiring.domain.ContractTemplateVariableEntity;
import com.agropay.core.hiring.domain.ContractTypeEntity;
import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.validation.domain.DynamicVariableEntity;
import com.agropay.core.validation.domain.DynamicVariableMethodEntity;
import com.agropay.core.validation.persistence.IDynamicVariableMethodRepository;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.hiring.mapper.IContractTemplateMapper;
import com.agropay.core.hiring.model.contracttemplate.*;
import com.agropay.core.hiring.model.variable.AssociatedMethodDTO;
import com.agropay.core.hiring.persistence.ContractTemplateSpecification;
import com.agropay.core.hiring.persistence.IContractTemplateRepository;
import com.agropay.core.hiring.persistence.IContractTemplateVariableRepository;
import com.agropay.core.hiring.persistence.IVariableRepository;
import com.agropay.core.states.application.IStateUseCase;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.exceptions.InvalidSortFieldException;
import com.agropay.core.shared.exceptions.NoChangesDetectedException;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractTemplateServiceImpl implements IContractTemplateUseCase {
    private final String deletedBy = "anonymous";
    
    
    private final IContractTemplateRepository contractTemplateRepository;
    private final IContractTemplateVariableRepository contractTemplateVariableRepository;
    private final IContractTypeUseCase contractTypeService;
    private final IStateUseCase stateService;
    private final IVariableRepository variableRepository;
    private final IDynamicVariableMethodRepository dynamicVariableMethodRepository;
    private final IContractTemplateMapper contractTemplateMapper;

    @Override
    @Transactional
    public CommandContractTemplateResponse create(CreateContractTemplateRequest request) {
        log.info("Attempting to create a new contract template with name: {}", request.name());
        if (contractTemplateRepository.existsByName(request.name())) {
            throw new UniqueValidationException("exception.hiring.contract-template.name-unique", request.name());
        }

        ContractTypeEntity contractType = contractTypeService.findByPublicId(request.contractTypePublicId());
        StateEntity state = stateService.findByPublicId(request.statePublicId());

        ContractTemplateEntity template = contractTemplateMapper.toEntity(request);
        template.setPublicId(UUID.randomUUID());
        template.setContractType(contractType);
        template.setState(state);

        String generatedCode = "CTMP_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) +
                "_" + String.format("%04d", new SecureRandom().nextInt(10000));
        template.setCode(generatedCode);

        handleVariableAssociations(template, request.variables());

        ContractTemplateEntity savedTemplate = contractTemplateRepository.save(template);
        log.info("Successfully created contract template with publicId: {}", savedTemplate.getPublicId());
        return contractTemplateMapper.toCommandResponse(savedTemplate);
    }

    @Override
    @Transactional
    public CommandContractTemplateResponse update(UUID publicId, UpdateContractTemplateRequest request) {
        log.info("Attempting to update contract template with publicId: {}", publicId);
        ContractTemplateEntity template = findByPublicId(publicId);

        if (isUpdateRedundant(request, template)) {
            throw new NoChangesDetectedException("exception.shared.no-changes-detected");
        }

        if (request.name() != null && !request.name().equals(template.getName())) {
            contractTemplateRepository.findByName(request.name()).ifPresent(existing -> {
                throw new UniqueValidationException("exception.hiring.contract-template.name-unique", request.name());
            });
        }

        contractTemplateMapper.updateEntityFromRequest(request, template);

        if (request.contractTypePublicId() != null && !request.contractTypePublicId().equals(template.getContractType().getPublicId())) {
            ContractTypeEntity newContractType = contractTypeService.findByPublicId(request.contractTypePublicId());
            template.setContractType(newContractType);
        }

        if (request.statePublicId() != null && !request.statePublicId().equals(template.getState().getPublicId())) {
            StateEntity newState = stateService.findByPublicId(request.statePublicId());
            template.setState(newState);
        }

        handleVariableAssociations(template, request.variables());

        ContractTemplateEntity updatedTemplate = contractTemplateRepository.save(template);
        log.info("Successfully updated contract template with publicId: {}", updatedTemplate.getPublicId());
        return contractTemplateMapper.toCommandResponse(updatedTemplate);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete contract template with publicId: {}", publicId);
        ContractTemplateEntity template = findByPublicId(publicId);

        List<ContractTemplateVariableEntity> variablesToDelete = new ArrayList<>(template.getVariables());

        variablesToDelete.forEach(variable ->
                contractTemplateVariableRepository.softDelete(variable.getId(), deletedBy));

        contractTemplateRepository.softDelete(template.getId(), deletedBy);

        log.info("Successfully deleted contract template with publicId: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandContractTemplateResponse getCommandResponseByPublicId(UUID publicId) {
        log.info("Fetching command response for contract template with publicId: {}", publicId);
        return contractTemplateMapper.toCommandResponse(findByPublicId(publicId));
    }

    @Override
    @Transactional(readOnly = true)
    public ContractTemplateContentDTO getContentByPublicId(UUID publicId) {
        log.info("Fetching content for contract template with publicId: {}", publicId);
        ContractTemplateEntity template = findByPublicId(publicId);

        // Procesar contenido: rellenar variables default y dejar no-default
        String processedContent = processTemplateContentWithDefaults(template);

        return new ContractTemplateContentDTO(
            template.getPublicId(),
            template.getCode(),
            template.getName(),
            processedContent, // ← Contenido procesado
            template.getCreatedAt(),
            template.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<ContractTemplateListDTO> findAllPaged(String code, String name, UUID contractTypePublicId, UUID statePublicId, Pageable pageable) {
        log.info("Fetching paged list of contract templates with filters: code={}, name='{}', contractTypePublicId={}, statePublicId={}, page={}, size={}", code, name, contractTypePublicId, statePublicId, pageable.getPageNumber(), pageable.getPageSize());

        Specification<ContractTemplateEntity> spec = ContractTemplateSpecification.filterBy(code, name, contractTypePublicId, statePublicId);
        Page<ContractTemplateEntity> templatePage = contractTemplateRepository.findAll(spec, pageable);
        return contractTemplateMapper.toPagedDTO(templatePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractTemplateSelectOptionDTO> getSelectOptions(UUID contractTypePublicId) {
        log.info("Fetching select options for contract templates with contractTypePublicId: {}", contractTypePublicId);
        List<ContractTemplateEntity> templates = contractTemplateRepository.findAllByContractType_PublicId(contractTypePublicId);
        return contractTemplateMapper.toSelectOptionDTOs(templates);
    }

    @Override
    public List<StateSelectOptionDTO> getStatesSelectOptions() {
        return stateService.findStateOptionsByDomainName(ContractTemplateEntity.TABLE_NAME);
    }

    @Transactional(readOnly = true)
    public ContractTemplateEntity findByPublicId(UUID publicId) {
        return contractTemplateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.contract-template.not-found", publicId));
    }

    private void handleVariableAssociations(ContractTemplateEntity template, List<ContractTemplateVariableRequest> variableRequests) {
        if (variableRequests != null) {
            template.getVariables().clear();
            if (!variableRequests.isEmpty()) {
                Set<ContractTemplateVariableEntity> newVariables = buildTemplateVariables(template, variableRequests);
                template.getVariables().addAll(newVariables);
            }
        }
    }

    private Set<ContractTemplateVariableEntity> buildTemplateVariables(ContractTemplateEntity template, List<ContractTemplateVariableRequest> variableRequests) {
        if (variableRequests == null || variableRequests.isEmpty()) {
            return Collections.emptySet();
        }
        return variableRequests.stream()
                .map(varRequest -> {
                    VariableEntity variable = variableRepository.findByPublicId(varRequest.variablePublicId())
                            .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.variable.not-found", varRequest.variablePublicId()));
                    return ContractTemplateVariableEntity.builder()
                            .contractTemplate(template)
                            .variable(variable)
                            .isRequired(varRequest.isRequired())
                            .displayOrder(varRequest.displayOrder())
                            .build();
                })
                .collect(Collectors.toSet());
    }

    private boolean isUpdateRedundant(UpdateContractTemplateRequest request, ContractTemplateEntity entity) {
        // If variables are not included in the request, we don't consider them for redundancy check.
        boolean variablesAreRedundant = request.variables() == null ||
                (request.variables().size() == entity.getVariables().size() &&
                        request.variables().stream().allMatch(reqVar ->
                                entity.getVariables().stream().anyMatch(entityVar ->
                                        entityVar.getVariable().getPublicId().equals(reqVar.variablePublicId()) &&
                                                entityVar.getIsRequired().equals(reqVar.isRequired()) &&
                                                entityVar.getDisplayOrder().equals(reqVar.displayOrder())
                                )
                        )
                );

        return Objects.equals(request.name(), entity.getName()) &&
                Objects.equals(request.templateContent(), entity.getTemplateContent()) &&
                Objects.equals(request.contractTypePublicId(), entity.getContractType().getPublicId()) &&
                Objects.equals(request.statePublicId(), entity.getState().getPublicId()) &&
                variablesAreRedundant;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractTemplateVariableWithValidationDTO> getVariablesWithValidationByPublicId(UUID publicId) {
        log.info("Fetching contract template variables with validation for publicId: {}", publicId);

        ContractTemplateEntity template = findByPublicId(publicId);
        List<ContractTemplateVariableEntity> templateVariables = contractTemplateVariableRepository
            .findByContractTemplateOrderByDisplayOrder(template);

        return templateVariables.stream()
            .filter(this::shouldShowInForm)
            .map(this::convertToVariableWithValidation)
            .toList();
    }

    /**
     * Determina si una variable debe mostrarse en el formulario del frontend.
     * Solo incluye variables que requieren input del usuario.
     */
    private boolean shouldShowInForm(ContractTemplateVariableEntity templateVariable) {
        VariableEntity variable = templateVariable.getVariable();

        // Solo mostrar variables requeridas
        if (!templateVariable.getIsRequired()) {
            return false;
        }

        // Excluir variables automáticas/por defecto basadas en código
        String code = variable.getCode().toUpperCase();
        return !isDefaultSystemVariable(code);
    }

    /**
     * Identifica variables que se llenan automáticamente por el sistema
     * (empresa, RUC, fechas automáticas, firmas, etc.)
     */
    private boolean isDefaultSystemVariable(String code) {
        return code.equals("COMPANY_NAME") ||
               code.equals("COMPANY_RUC") ||
               code.equals("COMPANY_ADDRESS") ||
               code.equals("CONTRACT_DATE") ||
               code.equals("CONTRACT_NUMBER") ||
               code.equals("CURRENT_DATE") ||
               code.equals("SYSTEM_USER") ||
               code.equals("FIRMA_ENCARGADO") || 
               code.equals("FIRMA_EMPLEADOR") ||
               code.startsWith("AUTO_") ||
               code.startsWith("SYSTEM_");
    }

    private ContractTemplateVariableWithValidationDTO convertToVariableWithValidation(ContractTemplateVariableEntity templateVariable) {
        VariableEntity variable = templateVariable.getVariable();
        DynamicVariableEntity dynamicVariable = variable.getDynamicVariable();

        ContractTemplateVariableWithValidationDTO.ValidationInfo validationInfo;

        if (dynamicVariable != null) {
            // La variable tiene validación dinámica
            List<DynamicVariableMethodEntity> methods = dynamicVariableMethodRepository
                .findByDynamicVariableOrderByExecutionOrder(dynamicVariable);

            List<AssociatedMethodDTO> appliedMethods = methods.stream()
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

            validationInfo = new ContractTemplateVariableWithValidationDTO.ValidationInfo(
                true,
                dynamicVariable.getFinalRegex(),
                dynamicVariable.getErrorMessage(),
                appliedMethods
            );
        } else {
            // La variable no tiene validación dinámica
            validationInfo = new ContractTemplateVariableWithValidationDTO.ValidationInfo(
                false,
                null,
                null,
                List.of()
            );
        }

        return new ContractTemplateVariableWithValidationDTO(
            variable.getPublicId(),
            variable.getCode(),
            variable.getName(),
            "string", // TODO: Agregar campo dataType a VariableEntity si es necesario
            variable.getDefaultValue(),
            templateVariable.getIsRequired(),
            templateVariable.getDisplayOrder(),
            validationInfo
        );
    }

    /**
     * Procesa el contenido de la plantilla rellenando variables default
     * y manteniendo placeholders para variables que requieren input del usuario.
     */
    private String processTemplateContentWithDefaults(ContractTemplateEntity template) {
        String content = template.getTemplateContent();
        List<ContractTemplateVariableEntity> templateVariables = contractTemplateVariableRepository
            .findByContractTemplateOrderByDisplayOrder(template);

        for (ContractTemplateVariableEntity templateVariable : templateVariables) {
            VariableEntity variable = templateVariable.getVariable();
            String placeholder = "{{" + variable.getCode() + "}}";

            if (isDefaultSystemVariable(variable.getCode().toUpperCase())) {
                // Es variable default - rellenar automáticamente
                String defaultValue = getDefaultVariableValue(variable.getCode());
                content = content.replace(placeholder, defaultValue != null ? defaultValue : "");
            }
            // Las variables no-default se mantienen como {{VARIABLE_NAME}} para el frontend
        }

        return content;
    }

    /**
     * Obtiene valores por defecto para variables del sistema.
     * En un entorno real, estos valores vendrían de configuración o servicios.
     */
    private String getDefaultVariableValue(String variableCode) {
        return switch (variableCode.toUpperCase()) {
            case "COMPANY_NAME" -> getCurrentCompanyName();
            case "COMPANY_RUC" -> getCurrentCompanyRuc();
            case "COMPANY_ADDRESS" -> getCurrentCompanyAddress();
            case "CONTRACT_DATE" -> getCurrentDate();
            case "CURRENT_DATE" -> getCurrentDate();
            case "SYSTEM_USER" -> getCurrentUser();
            default -> {
                if (variableCode.startsWith("AUTO_") || variableCode.startsWith("SYSTEM_")) {
                    yield getSystemGeneratedValue(variableCode);
                }
                yield null;
            }
        };
    }

    // Métodos helper para obtener valores del sistema
    private String getCurrentCompanyName() {
        // TODO: Integrar con servicio de empresa
        return "EMPRESA EJEMPLO S.A.C.";
    }

    private String getCurrentCompanyRuc() {
        // TODO: Integrar con servicio de empresa
        return "20123456789";
    }

    private String getCurrentCompanyAddress() {
        // TODO: Integrar con servicio de empresa
        return "Av. Ejemplo 123, Lima, Perú";
    }

    private String getCurrentDate() {
        return java.time.LocalDate.now().toString();
    }

    private String getCurrentUser() {
        // TODO: Integrar con contexto de seguridad
        return "Sistema";
    }

    private String getSystemGeneratedValue(String variableCode) {
        // TODO: Implementar generación de valores automáticos según el código
        return "[VALOR_GENERADO]";
    }
}
