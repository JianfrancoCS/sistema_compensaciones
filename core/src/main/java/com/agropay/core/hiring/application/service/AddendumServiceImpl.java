package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.application.usecase.IContractAddendumUseCase;
import com.agropay.core.hiring.application.usecase.IAddendumTypeUseCase;
import com.agropay.core.hiring.application.usecase.IAddendumTemplateUseCase;
import com.agropay.core.hiring.application.usecase.IAddendumVariableValueUseCase;
import com.agropay.core.hiring.application.usecase.IContractUseCase;
import com.agropay.core.organization.application.usecase.IEmployeeUseCase;
import com.agropay.core.hiring.domain.*;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.DataIntegrityViolationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.hiring.mapper.IContractAddendumMapper;
import com.agropay.core.hiring.model.addendum.*;
import com.agropay.core.hiring.persistence.ContractAddendumSpecification;
import com.agropay.core.hiring.persistence.IContractAddendumRepository;
import com.agropay.core.images.application.usecase.IFileStorageUseCase;
import com.agropay.core.images.application.usecase.IImageUseCase;
import com.agropay.core.images.constant.Bucket;
import com.agropay.core.images.domain.ImageEntity;
import com.agropay.core.images.model.SignatureUrlCommand;
import com.agropay.core.states.application.IStateUseCase;
import com.agropay.core.states.constant.AddendumStateEnum;
import com.agropay.core.shared.constant.VariablesEnum;
import com.agropay.core.states.constant.AddendumTypeEnum;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.exceptions.NoChangesDetectedException;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddendumServiceImpl implements IContractAddendumUseCase  {

    private final IContractAddendumRepository contractAddendumRepository;
    private final IContractAddendumMapper contractAddendumMapper;
    private final IContractUseCase contractService;
    private final IAddendumTypeUseCase addendumTypeService;
    private final IStateUseCase stateService;
    private final IAddendumVariableValueUseCase addendumVariableValueService;
    private final IAddendumTemplateUseCase addendumTemplateService;
    private final IFileStorageUseCase fileStorageService;
    private final IImageUseCase imageService;
    private final IEmployeeUseCase employeeService;

    @Override
    @Transactional
    public CommandAddendumResponse create(CreateAddendumRequest request) {
        log.info("Attempting to create addendum for contract={}", request.contractPublicId());

        ContractEntity contract = contractService.findByPublicId(request.contractPublicId());
        AddendumTypeEntity addendumType = addendumTypeService.findByPublicId(request.addendumTypePublicId());
        AddendumTemplateEntity addendumTemplate = addendumTemplateService.findByPublicId(request.templatePublicId());

        // Validate that no addendum of this type already exists for this contract
        if (contractAddendumRepository.existsByContract_PublicIdAndAddendumType_PublicId(
                request.contractPublicId(), request.addendumTypePublicId())) {
            throw new UniqueValidationException(
                "exception.hiring.addendum.duplicate-type-for-contract",
                addendumType.getName(), contract.getContractNumber()
            );
        }

        StateEntity state = stateService.findDefaultStateByDomainName(AddendumEntity.TABLE_NAME);

        String addendumNumber = generateAddendumNumber();
        LocalDate effectiveDate = request.startDate();

        Map<String, String> providedValues = request.variables() != null ?
                request.variables().stream()
                        .collect(Collectors.toMap(AddendumVariableValuePayload::code, AddendumVariableValuePayload::value))
                : new HashMap<>();

        List<String> contentVars = new ArrayList<>(providedValues.keySet());

        // Solo obtener variables que son REQUERIDAS para este template específico
        // y que NO tienen valores por defecto
        List<String> requiredVarsWithoutDefaults = addendumTemplate.getVariables()
                .stream()
                .filter(at -> at.getIsRequired()) // Solo las requeridas en este template
                .filter(at -> at.getVariable().getDefaultValue() == null || at.getVariable().getDefaultValue().trim().isEmpty()) // Sin valores por defecto
                .map(at -> at.getVariable().getCode())
                .toList();

        validateRequiredVariables(requiredVarsWithoutDefaults, contentVars);

        AddendumEntity addendum = contractAddendumMapper.toEntity(request);
        addendum.setPublicId(UUID.randomUUID());
        addendum.setAddendumNumber(addendumNumber);
        addendum.setEffectiveDate(effectiveDate);
        addendum.setContract(contract);
        addendum.setAddendumType(addendumType);
        addendum.setState(state);
        addendum.setContent(addendumTemplate.getTemplateContent());
        addendum.setTemplate(addendumTemplate);

        processAddendumEffects(addendum, providedValues);

        AddendumEntity savedAddendum = contractAddendumRepository.save(addendum);

        if (!providedValues.isEmpty()) {
            addendumVariableValueService.create(savedAddendum, providedValues);
        }

        log.info("Addendum created: number={}, publicId={}, contract={}",
                savedAddendum.getAddendumNumber(), savedAddendum.getPublicId(), request.contractPublicId());

        return contractAddendumMapper.toCommandResponse(savedAddendum);
    }

    private void processAddendumEffects(AddendumEntity addendum, Map<String, String> providedValues) {
        AddendumTypeEnum addendumTypeEnum = Arrays.stream(AddendumTypeEnum.values())
                .filter(type -> type.getCode().equals(addendum.getAddendumType().getCode()))
                .findFirst()
                .orElse(null);

        if (addendumTypeEnum == null) {
            log.warn("Unknown addendum type code: {}. No special processing will be applied.", addendum.getAddendumType().getCode());
            return;
        }

        switch (addendumTypeEnum) {
            case ECONOMIC:
                providedValues.entrySet().stream()
                        .filter(entry -> VariablesEnum.NUEVO_SALARIO.getCode().equals(entry.getKey()))
                        .findFirst()
                        .ifPresent(entry -> {
                            String employeeDocNumber = addendum.getContract().getPersonDocumentNumber();
                            BigDecimal newSalary = new BigDecimal(entry.getValue());
                            // Actualizar salario del empleado directamente (sin evento)
                            employeeService.updateCustomSalary(employeeDocNumber, newSalary);
                            log.info("Updated custom salary for employee {}", employeeDocNumber);
                        });
                break;
            case DURATION:
                providedValues.entrySet().stream()
                        .filter(entry -> VariablesEnum.NUEVA_FECHA_FIN.getCode().equals(entry.getKey()))
                        .findFirst()
                        .ifPresent(entry -> {
                            LocalDate newEndDate = LocalDate.parse(entry.getValue());
                            addendum.setNewEndDate(newEndDate);
                            log.info("Set new_end_date on addendum {} for contract {}", addendum.getPublicId(), addendum.getContract().getPublicId());
                        });
                break;
            case SCOPE:
            case CONDITIONS:
                log.debug("Addendum type {} requires no special processing at this time.", addendumTypeEnum);
                break;
            default:
                log.warn("Unhandled addendum type: {}. No special processing will be applied.", addendumTypeEnum);
                break;
        }
    }

    @Override
    @Transactional
    public CommandAddendumResponse update(UUID publicId, UpdateAddendumRequest request) {
        log.info("Attempting to update addendum with publicId: {}", publicId);
        AddendumEntity addendum = findByPublicId(publicId);

        if (addendum.getState().getCode().equals(AddendumStateEnum.SIGNED.getCode())) {
            throw new BusinessValidationException("exception.hiring.addendum.signed-cannot-be-updated");
        }

        if (isUpdateRedundant(request, addendum)) {
            throw new NoChangesDetectedException("exception.shared.no-changes-detected");
        }

        StateEntity defaultState = stateService.findDefaultStateByDomainName(AddendumEntity.TABLE_NAME);
        if (!addendum.getState().getId().equals(defaultState.getId())) {
            throw new IllegalStateException("Addendum can only be updated in draft state.");
        }

        contractAddendumMapper.updateEntityFromRequest(request, addendum);

        if (request.addendumTypePublicId() != null && !request.addendumTypePublicId().equals(addendum.getAddendumType().getPublicId())) {
            AddendumTypeEntity newAddendumType = addendumTypeService.findByPublicId(request.addendumTypePublicId());
            addendum.setAddendumType(newAddendumType);
        }

        if (request.statePublicId() != null && !request.statePublicId().equals(addendum.getState().getPublicId())) {
            StateEntity newState = stateService.findByPublicId(request.statePublicId());
            addendum.setState(newState);
        }

        if (request.templatePublicId() != null && !request.templatePublicId().equals(addendum.getTemplate().getPublicId())) {
            AddendumTemplateEntity newTemplate = addendumTemplateService.findByPublicId(request.templatePublicId());
            addendum.setTemplate(newTemplate);
            addendum.setContent(newTemplate.getTemplateContent());
        }

        Map<String, String> providedValues = request.variables() != null ?
                request.variables().stream()
                        .collect(Collectors.toMap(AddendumVariableValuePayload::code, AddendumVariableValuePayload::value))
                : new HashMap<>();

        // Solo obtener variables que son REQUERIDAS para este template específico
        // y que NO tienen valores por defecto
        List<String> requiredVarsWithoutDefaults = addendum.getTemplate().getVariables()
                .stream()
                .filter(at -> at.getIsRequired()) // Solo las requeridas en este template
                .filter(at -> at.getVariable().getDefaultValue() == null || at.getVariable().getDefaultValue().trim().isEmpty()) // Sin valores por defecto
                .map(at -> at.getVariable().getCode())
                .toList();

        List<String> contentVars = new ArrayList<>(providedValues.keySet());

        validateRequiredVariables(requiredVarsWithoutDefaults, contentVars);

        addendumVariableValueService.update(addendum, providedValues);

        AddendumEntity updatedAddendum = contractAddendumRepository.save(addendum);
        log.info("Successfully updated addendum with publicId: {}", updatedAddendum.getPublicId());
        return contractAddendumMapper.toCommandResponse(updatedAddendum);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete addendum with publicId: {}", publicId);
        AddendumEntity addendum = findByPublicId(publicId);

        if (!addendum.getChildAddendums().isEmpty()) {
            throw new IllegalStateException("Cannot delete addendum with child addendums");
        }
        // Usar getEntityId() que devuelve Long (getId() devuelve String para IImageable)
        contractAddendumRepository.softDelete(addendum.getEntityId(), "anonymous");
        log.info("Successfully deleted addendum with publicId: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public AddendumDetailsDTO getDetailsByPublicId(UUID publicId) {
        log.info("Fetching details for addendum with publicId: {}", publicId);
        AddendumEntity addendum = findByPublicId(publicId);

        String imageUrl = null;
        List<ImageEntity> images = imageService.getImagesByImageable(addendum);
        if (images != null && !images.isEmpty()) {
            imageUrl = images.getFirst().getUrl();
        }

        return contractAddendumMapper.toDetailsDTO(addendum, imageUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandAddendumResponse getCommandResponseByPublicId(UUID publicId) {
        AddendumEntity addendum = this.findByPublicId(publicId);
        return contractAddendumMapper.toCommandResponse(addendum);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<AddendumListDTO> findAllPaged(String addendumNumber, UUID contractPublicId, Pageable pageable) {
        log.info("Fetching paged list of addendums with filters: addendumNumber='{}', contractPublicId={}, page={}, size={}",
                addendumNumber, contractPublicId, pageable.getPageNumber(), pageable.getPageSize());

        Specification<AddendumEntity> spec = ContractAddendumSpecification.filterBy(addendumNumber, contractPublicId, null, null);

        Page<AddendumEntity> addendumPage = contractAddendumRepository.findAll(spec, pageable);

        List<AddendumListDTO> dtoList = addendumPage.getContent().stream()
                .map(contractAddendumMapper::toListDTO)
                .toList();

        return new PagedResult<>(dtoList,
                addendumPage.getTotalElements(),
                addendumPage.getNumber(),
                addendumPage.getTotalPages(),
                addendumPage.isFirst(),
                addendumPage.isLast(),
                addendumPage.hasNext(),
                addendumPage.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddendumListDTO> getByContractPublicId(UUID contractPublicId) {
        log.info("Fetching addendums for contract with publicId: {}", contractPublicId);
        List<AddendumEntity> addendums = contractAddendumRepository.findByContract_PublicIdOrderByCreatedAtDesc(contractPublicId);
        return addendums.stream()
                .map(contractAddendumMapper::toListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddendumContentDTO getContentByPublicId(UUID publicId) {
        log.info("Generating addendum content for publicId: {}", publicId);
        AddendumEntity addendum = findByPublicId(publicId);

        Map<String, String> variableValuesMap = addendum.getVariableValues().stream()
                .collect(Collectors.toMap(
                        v -> v.getVariable().getCode(),
                        AddendumVariableValueEntity::getValue
                ));

        String mergedContent = populateContent(addendum.getContent(), variableValuesMap);

        return new AddendumContentDTO(
                addendum.getPublicId(),
                addendum.getAddendumNumber(),
                mergedContent,
                addendum.getCreatedAt(),
                addendum.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public CommandAddendumResponse signAddendum(UUID publicId, SignAddendumRequest request) {
        log.info("Attempting to sign addendum with publicId: {}", publicId);
        AddendumEntity addendum = findByPublicId(publicId);

        StateEntity signedState = stateService.findByCodeAndDomainName(AddendumStateEnum.SIGNED.getCode(), AddendumEntity.TABLE_NAME);
        addendum.setState(signedState);

        if (request.imagesUri() != null && !request.imagesUri().trim().isEmpty()) {
            imageService.attachImage(addendum, request.imagesUri());
            log.info("Attached signed image {} to addendum {}", request.imagesUri(), publicId);
        }

        AddendumEntity savedAddendum = contractAddendumRepository.save(addendum);

        processSignedAddendumEffects(addendum);

        log.info("Addendum {} signed successfully.", publicId);
        return contractAddendumMapper.toCommandAddendumResponse(savedAddendum);
    }

    private void processSignedAddendumEffects(AddendumEntity addendum) {
        AddendumTypeEnum addendumTypeEnum = Arrays.stream(AddendumTypeEnum.values())
                .filter(type -> type.getCode().equals(addendum.getAddendumType().getCode()))
                .findFirst()
                .orElse(null);

        if (addendumTypeEnum == null) {
            log.warn("Unknown addendum type code: {}. No special processing will be applied.", addendum.getAddendumType().getCode());
            return;
        }

        switch (addendumTypeEnum) {
            case ECONOMIC:
                if (addendum.getNewSalary() != null) {
                    // Actualizar salario del empleado directamente (sin evento)
                    employeeService.updateCustomSalary(
                        addendum.getContract().getPersonDocumentNumber(),
                        addendum.getNewSalary()
                    );
                    log.info("Updated custom salary for employee {} with new salary {}",
                            addendum.getContract().getPersonDocumentNumber(),
                            addendum.getNewSalary());
                }
                break;
            case DURATION:
                if (addendum.getNewEndDate() != null) {
                    // Actualizar fecha de extensión del contrato directamente (sin evento)
                    contractService.updateExtendedEndDate(
                        addendum.getContract().getPublicId(),
                        addendum.getNewEndDate()
                    );
                    log.info("Updated extended end date for contract {} with new end date {}",
                            addendum.getContract().getPublicId(),
                            addendum.getNewEndDate());
                }
                break;
            case SCOPE:
            case CONDITIONS:
                log.debug("Addendum type {} requires no special processing when signed.", addendumTypeEnum);
                break;
            default:
                log.warn("Unhandled addendum type: {}. No special processing will be applied.", addendumTypeEnum);
                break;
        }
    }

    @Override
    @Transactional
    public UploadUrlResponse generateUploadUrl(GenerateUploadUrlRequest request) {
        log.info("Generating upload URL for addendum");
        try {
            SignatureUrlCommand signatureParams = fileStorageService.getSignature(Bucket.CONTRACT_DOCUMENT);
            String uploadUrl = signatureParams.uploadUrl();

            return new UploadUrlResponse(uploadUrl);
        } catch (Exception e) {
            log.error("Error generating upload URL for addendum: {}", e.getMessage());
            throw new RuntimeException("Error generating upload URL", e);
        }
    }

    @Override
    @Transactional
    public CommandAddendumResponse attachFile(UUID publicId, AttachFileRequest request) {
        log.info("Attaching file to addendum with publicId: {}", publicId);
        AddendumEntity addendum = findByPublicId(publicId);
        imageService.attachImage(addendum, request.fileUrl());
        log.info("Attached new file {} to addendum {}", request.fileUrl(), publicId);
        return contractAddendumMapper.toCommandAddendumResponse(addendum);
    }

    @Override
    public List<StateSelectOptionDTO> getStatesSelectOptions() {
        return stateService.findStateOptionsByDomainName(AddendumEntity.TABLE_NAME);
    }



    public AddendumEntity findByPublicId(UUID publicId) {
        return contractAddendumRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.addendum.not-found", publicId));
    }

    private String generateAddendumNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = String.format("%04d", new SecureRandom().nextInt(10000));
        return "AD_" + datePart + "_" + randomPart;
    }

    private void validateRequiredVariables(List<String> templateVariables, List<String> providedVariableCodes) {
        List<String> missingVariables = new ArrayList<>();
        for (String variable : templateVariables) {
            if (!providedVariableCodes.contains(variable)) {
                missingVariables.add(variable);
            }
        }
        if (!missingVariables.isEmpty()) {
            String missingVarsString = String.join(", ", missingVariables);
            throw new DataIntegrityViolationException(
                    "exception.hiring.missing-required-variables",
                    new Object[]{missingVarsString}
            );
        }
    }

    private String populateContent(String templateContent, Map<String, String> providedValues) {
        String result = templateContent;
        for (Map.Entry<String, String> entry : providedValues.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }

    private boolean isUpdateRedundant(UpdateAddendumRequest request, AddendumEntity entity) {
        Map<String, String> currentVariables = entity.getVariableValues().stream()
                .collect(Collectors.toMap(v -> v.getVariable().getCode(), AddendumVariableValueEntity::getValue));

        Map<String, String> newVariables = request.variables() != null ?
                request.variables().stream()
                        .collect(Collectors.toMap(AddendumVariableValuePayload::code, AddendumVariableValuePayload::value))
                : new HashMap<>();

        return Objects.equals(request.addendumTypePublicId(), entity.getAddendumType().getPublicId()) &&
                Objects.equals(request.statePublicId(), entity.getState().getPublicId()) &&
                Objects.equals(request.templatePublicId(), entity.getTemplate().getPublicId()) &&
                Objects.equals(currentVariables, newVariables);
    }
}
