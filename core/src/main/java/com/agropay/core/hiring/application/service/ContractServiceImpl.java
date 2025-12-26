package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.application.usecase.IContractUseCase;
import com.agropay.core.hiring.application.usecase.IContractTypeUseCase;
import com.agropay.core.hiring.application.usecase.IContractTemplateUseCase;
import com.agropay.core.hiring.application.usecase.IContractVariableValueUseCase;
import com.agropay.core.hiring.constant.ContractTypeEnum;
import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.hiring.domain.ContractTypeEntity;
import com.agropay.core.hiring.domain.ContractTemplateEntity;
import com.agropay.core.hiring.domain.ContractVariableValueEntity;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.DataIntegrityViolationException;
import com.agropay.core.hiring.mapper.IContractMapper;
import com.agropay.core.hiring.model.contract.*;
import com.agropay.core.hiring.persistence.ContractSpecification;
import com.agropay.core.hiring.persistence.IContractRepository;
import com.agropay.core.hiring.persistence.IContractVariableValueRepository;
import com.agropay.core.hiring.persistence.IVariableRepository;
import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.hiring.domain.ContractVariableValueId;
import com.agropay.core.files.application.usecase.IInternalFileStorageUseCase;
import com.agropay.core.files.constant.FileCategory;
import com.agropay.core.files.domain.InternalFileEntity;
import com.agropay.core.images.application.usecase.IFileStorageUseCase;
import com.agropay.core.images.application.usecase.IImageUseCase;
import com.agropay.core.images.domain.ImageEntity;
import com.agropay.core.images.persistence.ImageRepository;
import com.agropay.core.images.constant.Bucket;
import com.agropay.core.organization.api.IPersonAPI;
import com.agropay.core.organization.api.IPositionAPI;
import com.agropay.core.organization.api.ISubsidiaryAPI;
import com.agropay.core.organization.application.usecase.ICompanyUseCase;
import com.agropay.core.organization.application.usecase.IEmployeeUseCase;
import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.organization.domain.CompanySubsidiarySignerEntity;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.organization.persistence.ICompanySubsidiarySignerRepository;
import com.agropay.core.organization.persistence.IEmployeeRepository;
import com.agropay.core.organization.model.employee.CreateEmployeeRequest;
import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.persistence.IConceptRepository;
import com.agropay.core.states.application.IStateUseCase;
import com.agropay.core.states.constant.ContractStateEnum;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.exceptions.NoChangesDetectedException;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.PagedResult;
import com.agropay.core.hiring.validation.VariableValidator;
import com.agropay.core.auth.domain.UserEntity;
import com.agropay.core.auth.domain.UserProfileEntity;
import com.agropay.core.auth.domain.ProfileEntity;
import com.agropay.core.auth.persistence.IUserRepository;
import com.agropay.core.auth.persistence.IProfileRepository;
import com.agropay.core.auth.persistence.IUserProfileRepository;
import com.agropay.core.auth.constant.BaseProfileEnum;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements IContractUseCase {

    private static final int MIN_CONTRACT_DURATION_DAYS = 7;

    private final IContractRepository contractRepository;
    private final IContractMapper contractMapper;
    private final IPersonAPI personAPI;
    private final IContractTypeUseCase contractTypeService;
    private final IStateUseCase stateService;
    private final ISubsidiaryAPI subsidiaryUseCase;
    private final IPositionAPI positionUseCase;
    private final IContractVariableValueUseCase contractVariableValueService;
    private final IContractVariableValueRepository contractVariableValueRepository;
    private final IContractTemplateUseCase contractTemplateService;
    private final IFileStorageUseCase fileStorageService; // Mantener para compatibilidad, pero deprecado
    private final IImageUseCase imageService; // Mantener para compatibilidad con código legacy
    private final ImageRepository imageRepository; // Mantener para compatibilidad con código legacy
    private final IInternalFileStorageUseCase internalFileStorageService; // Nuevo servicio para archivos internos
    private final VariableValidator variableValidator;
    private final IEmployeeUseCase employeeService;
    private final ICompanyUseCase companyUseCase;
    private final ICompanySubsidiarySignerRepository companySubsidiarySignerRepository;
    private final com.agropay.core.hiring.application.usecase.IContractPdfService contractPdfService;
    private final IUserRepository userRepository;
    private final IProfileRepository profileRepository;
    private final IUserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final IConceptRepository conceptRepository;
    private final IEmployeeRepository employeeRepository;
    private final IVariableRepository variableRepository;

    @Override
    @Transactional
    public CommandContractResponse create(CreateContractRequest request) {
        throw new BusinessValidationException("exception.hiring.contract.photo-required", 
                "La foto es obligatoria. Use el método create(request, photo)");
    }

    @Override
    @Transactional
    public CommandContractResponse create(CreateContractRequest request, MultipartFile photo) {

        log.info("Attempting to create contract for person={} with photo", request.personDocumentNumber());
        
        if (photo == null || photo.isEmpty()) {
            throw new BusinessValidationException("exception.hiring.contract.photo-required", 
                    "La foto es obligatoria para crear un contrato");
        }

        PersonEntity orCreatePersonByDni = personAPI.findOrCreatePersonByDni(request.personDocumentNumber());

        try {
            log.info("Subiendo foto para persona {} al crear contrato", orCreatePersonByDni.getDocumentNumber());
            Map<String, Object> uploadResult = fileStorageService.uploadFile(photo, Bucket.PERSON_PHOTO);
            String imageUrl = (String) uploadResult.get("secure_url");
            if (imageUrl == null) {
                imageUrl = (String) uploadResult.get("url");
            }
            
            imageService.attachImage(orCreatePersonByDni, imageUrl);
            log.info("Foto asociada exitosamente a persona {}. URL: {}", orCreatePersonByDni.getDocumentNumber(), imageUrl);
        } catch (Exception e) {
            log.error("Error subiendo foto para persona {}: {}", orCreatePersonByDni.getDocumentNumber(), e.getMessage(), e);
            throw new BusinessValidationException("exception.hiring.contract.photo-upload-error", 
                    "Error al subir la foto: " + e.getMessage());
        }

        StateEntity state = stateService.findDefaultStateByDomainName(ContractEntity.TABLE_NAME);
        SubsidiaryEntity subsidiary = subsidiaryUseCase.findByPublicId(request.subsidiaryPublicId());
        
        validateSubsidiarySigner(subsidiary);
        
        PositionEntity position = positionUseCase.findByPublicId(request.positionPublicId());

        ContractTypeEntity contractType = contractTypeService.findByPublicId(request.contractTypePublicId());
        ContractTemplateEntity contractTemplate = contractTemplateService.findByPublicId(request.templatePublicId());

        LocalDate endDate = validateContractTypeAndEndDate(contractType, request.endDate());

        String contractNumber = generateContractNumber();
        LocalDate startDate = LocalDate.now();

        Map<String, String> providedValues = request.variables().stream()
                .collect(Collectors.toMap(ContractVariableValuePayload::code, ContractVariableValuePayload::value));

        List<String> requiredVarsWithoutDefaults = contractTemplate.getVariables()
                .stream()
                .filter(ct -> ct.getIsRequired())
                .filter(ct -> ct.getVariable().getDefaultValue() == null || ct.getVariable().getDefaultValue().trim().isEmpty()) // Sin valores por defecto
                .map(ct -> ct.getVariable().getCode())
                .toList();

        validateRequiredNonDefaultVariables(requiredVarsWithoutDefaults, providedValues.keySet());

        variableValidator.validateVariablesForTemplate(contractTemplate, providedValues);

        ContractEntity contract = contractMapper.toEntity(request);
        contract.setPublicId(UUID.randomUUID());
        contract.setContractNumber(contractNumber);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setPersonDocumentNumber(orCreatePersonByDni.getDocumentNumber());
        contract.setContractType(contractType);
        contract.setState(state);
        contract.setContent(contractTemplate.getTemplateContent());
        contract.setPosition(position);
        contract.setSubsidiary(subsidiary);
        contract.setTemplate(contractTemplate);

        ContractEntity savedContract = contractRepository.save(contract);

        List<String> allTemplateVars = contractTemplate.getVariables()
                .stream()
                .map(ct -> ct.getVariable().getCode())
                .toList();

        Map<String, String> allVariables = combineVariablesForStorage(allTemplateVars, providedValues, savedContract);
        
        if (allTemplateVars.contains("FIRMA_ENCARGADO")) {
            String encargadoSignatureUrl = getSubsidiarySignerSignatureUrl(subsidiary);
            if (encargadoSignatureUrl != null) {
                allVariables.put("FIRMA_ENCARGADO", encargadoSignatureUrl);
            }
        }
        
        Map<String, String> templateVariables = new HashMap<>(allVariables);
        Map<String, String> internalVariables = new HashMap<>();
        
        if (request.retirementConceptPublicId() != null) {
            internalVariables.put("_INTERNAL_RETIREMENT_CONCEPT_PUBLIC_ID", request.retirementConceptPublicId().toString());
            templateVariables.remove("_INTERNAL_RETIREMENT_CONCEPT_PUBLIC_ID");
        }
        if (request.healthInsuranceConceptPublicId() != null) {
            internalVariables.put("_INTERNAL_HEALTH_INSURANCE_CONCEPT_PUBLIC_ID", request.healthInsuranceConceptPublicId().toString());
            templateVariables.remove("_INTERNAL_HEALTH_INSURANCE_CONCEPT_PUBLIC_ID");
        }
        
        contractVariableValueService.create(savedContract, templateVariables);
        
        internalVariables.forEach((code, value) -> {
            VariableEntity variable = variableRepository.findByCode(code).orElseGet(() -> {
                VariableEntity newVariable = VariableEntity.builder()
                        .publicId(UUID.randomUUID())
                        .code(code)
                        .name("Variable interna: " + code)
                        .defaultValue(null)
                        .build();
                return variableRepository.save(newVariable);
            });
            
            ContractVariableValueId id = new ContractVariableValueId(savedContract.getEntityId(), variable.getId());
            ContractVariableValueEntity contractVariableValue = new ContractVariableValueEntity();
            contractVariableValue.setId(id);
            contractVariableValue.setContract(savedContract);
            contractVariableValue.setVariable(variable);
            contractVariableValue.setValue(value);
            
            contractVariableValueRepository.save(contractVariableValue);
        });

        log.info("Contract created for person {}. Employee will be created/updated when contract is signed.", 
                orCreatePersonByDni.getDocumentNumber());

        try {
            log.info("Iniciando generación de PDF inicial para contrato {} (ID: {})", savedContract.getPublicId(), savedContract.getEntityId());
            
            ContractEntity contractWithVars = contractRepository.findById(savedContract.getEntityId())
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.contract.not-found", savedContract.getPublicId().toString()));
            
            log.info("Contrato recargado. ID como String para IFileable: {}", contractWithVars.getId());
            
            ContractContentDTO contractContent = getContractContent(savedContract.getPublicId());
            String htmlContent = contractContent.mergedContent();
            log.info("Contenido HTML obtenido. Longitud: {} caracteres", htmlContent != null ? htmlContent.length() : 0);
            
            contractPdfService.generateAndSaveContractPdf(contractWithVars, htmlContent, false);
            log.info("PDF inicial generado y guardado exitosamente para contrato {}", savedContract.getPublicId());
        } catch (Exception e) {
            log.error("ERROR generando PDF inicial para contrato {} (ID: {}): {}", 
                    savedContract.getPublicId(), savedContract.getEntityId(), e.getMessage(), e);
            log.error("Stack trace completo del error:", e);
        }

        log.info("Contract created: number={}, publicId={}, person={}",
                savedContract.getContractNumber(), savedContract.getPublicId(), request.personDocumentNumber());

        return contractMapper.toCommandResponse(savedContract);
    }

    @Override
    @Transactional
    public CommandContractResponse update(UUID publicId, UpdateContractRequest request) {
        log.info("Attempting to update contract with publicId: {}", publicId);
        ContractEntity contract = findByPublicId(publicId);

        if (contract.getState().getCode().equals(ContractStateEnum.SIGNED.getCode())) {
            throw new BusinessValidationException("exception.hiring.contract.signed-cannot-be-updated");
        }

        if (isUpdateRedundant(request, contract)) {
            throw new NoChangesDetectedException("exception.shared.no-changes-detected");
        }

        StateEntity defaultState = stateService.findDefaultStateByDomainName(ContractEntity.TABLE_NAME);
        if (!contract.getState().getId().equals(defaultState.getId())) {
            throw new IllegalStateException("Contract can only be updated in draft state.");
        }

        contractMapper.updateEntityFromRequest(request, contract);

        Map<String, String> providedValues = request.variables().stream()
                .collect(Collectors.toMap(ContractVariableValuePayload::code, ContractVariableValuePayload::value));

        List<String> requiredVarsWithoutDefaults = contract.getTemplate().getVariables()
                .stream()
                .filter(ct -> ct.getIsRequired()) // Solo las requeridas en este template
                .filter(ct -> ct.getVariable().getDefaultValue() == null || ct.getVariable().getDefaultValue().trim().isEmpty()) // Sin valores por defecto
                .map(ct -> ct.getVariable().getCode())
                .toList();

        validateRequiredNonDefaultVariables(requiredVarsWithoutDefaults, providedValues.keySet());

        variableValidator.validateVariablesForTemplate(contract.getTemplate(), providedValues);

        List<String> allTemplateVars = contract.getTemplate().getVariables()
                .stream()
                .map(ct -> ct.getVariable().getCode())
                .toList();

        Map<String, String> allVariables = combineVariablesForStorage(allTemplateVars, providedValues, contract);
        contractVariableValueService.update(contract, allVariables);

        ContractEntity updatedContract = contractRepository.save(contract);
        log.info("Successfully updated contract with publicId: {}", updatedContract.getPublicId());
        return contractMapper.toCommandResponse(updatedContract);
    }


    @Override
    @Transactional(readOnly = true)
    public ContractDetailsDTO getDetailsByPublicId(UUID publicId) {
        log.info("Fetching details for contract with publicId: {}", publicId);
        ContractEntity contract = findByPublicId(publicId);

        boolean isSigned = ContractStateEnum.SIGNED.getCode().equals(contract.getState().getCode());
        String categoryToFetch = isSigned ? FileCategory.CONTRACT_SIGNED.getCode() : FileCategory.CONTRACT_INITIAL.getCode();
        
        log.info("Contract {} is signed: {}. Fetching PDF from category: {}", publicId, isSigned, categoryToFetch);
        
        List<InternalFileEntity> internalFiles = internalFileStorageService.getFilesByFileableAndCategory(
                contract, categoryToFetch);
        
        if (internalFiles.isEmpty()) {
            String fallbackCategory = isSigned ? FileCategory.CONTRACT_INITIAL.getCode() : FileCategory.CONTRACT_SIGNED.getCode();
            log.warn("No PDF found in category {} for contract {}. Trying fallback category: {}", 
                    categoryToFetch, publicId, fallbackCategory);
            internalFiles = internalFileStorageService.getFilesByFileableAndCategory(contract, fallbackCategory);
        }
        
        if (internalFiles.isEmpty()) {
            log.warn("No PDF found in specific categories for contract {}. Trying generic CONTRACT category.", publicId);
            internalFiles = internalFileStorageService.getFilesByFileableAndCategory(contract, FileCategory.CONTRACT.getCode());
        }
        
        List<ContractDetailsDTO.ContractImageDTO> imageUrls = internalFiles.stream()
                .map(file -> {
                    String downloadUrl = "/v1/internal-files/" + file.getPublicId() + "/download";
                    return new ContractDetailsDTO.ContractImageDTO(
                            file.getPublicId(), 
                            downloadUrl, 
                            file.getId().intValue() // Usar ID como orden temporal
                    );
                })
                .sorted(Comparator.comparing(ContractDetailsDTO.ContractImageDTO::order))
                .toList();

        return contractMapper.toDetailsDTO(contract, imageUrls);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandContractResponse getCommandResponseByPublicId(UUID publicId) {
        ContractEntity contract = this.findByPublicId(publicId);
        return contractMapper.toCommandResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<ContractListDTO> findAllPaged(String contractNumber, String personDocumentNumber,
                                                     UUID contractTypePublicId, UUID statePublicId, Pageable pageable) {
        log.info("Fetching paged list of contracts with filters: contractNumber='{}', personDocumentNumber='{}', contractTypePublicId={}, statePublicId={}, page={}, size={}",
                contractNumber, personDocumentNumber, contractTypePublicId, statePublicId, pageable.getPageNumber(), pageable.getPageSize());

        Specification<ContractEntity> spec = ContractSpecification.filterBy(contractNumber, personDocumentNumber, contractTypePublicId, statePublicId);

        Page<ContractEntity> contractPage = contractRepository.findAll(spec, pageable);

        List<ContractListDTO> dtoList = contractPage.getContent().stream()
                .map(contract -> {
                    boolean isSigned = ContractStateEnum.SIGNED.getCode().equals(contract.getState().getCode());
                    List<InternalFileEntity> contractFiles = internalFileStorageService.getFilesByFileableAndCategory(
                            contract, FileCategory.CONTRACT.getCode());
                    boolean hasFiles = !contractFiles.isEmpty();

                    return contractMapper.toListDTO(contract, isSigned, hasFiles);
                })
                .toList();

        return new PagedResult<>(dtoList,
                contractPage.getTotalElements(),
                contractPage.getNumber(),
                contractPage.getTotalPages(),
                contractPage.isFirst(),
                contractPage.isLast(),
                contractPage.hasNext(),
                contractPage.hasPrevious()
        );
    }

    @Override
    public List<StateSelectOptionDTO> getStatesSelectOptions() {
        return stateService.findStateOptionsByDomainName(ContractEntity.TABLE_NAME);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractContentDTO getContractContent(UUID publicId) {
        log.info("Generating contract content for publicId: {}", publicId);
        ContractEntity contract = findByPublicId(publicId);

        Map<String, String> allVariablesMap = contract.getTemplate().getVariables().stream()
                .collect(Collectors.toMap(
                        tv -> tv.getVariable().getCode(),
                        tv -> tv.getVariable().getDefaultValue() != null ? tv.getVariable().getDefaultValue() : ""
                ));

        Map<String, String> contractVariablesMap = new HashMap<>();
        if (contract.getVariableValues() != null && !contract.getVariableValues().isEmpty()) {
            contractVariablesMap = contract.getVariableValues().stream()
                    .collect(Collectors.toMap(
                            v -> v.getVariable().getCode(),
                            ContractVariableValueEntity::getValue
                    ));
        } else {
            log.warn("VariableValues is null or empty for contract {}. Loading from repository.", contract.getPublicId());
            List<ContractVariableValueEntity> loadedVariables = contractVariableValueRepository.findByContractId(contract.getEntityId());
            if (loadedVariables != null && !loadedVariables.isEmpty()) {
                contractVariablesMap = loadedVariables.stream()
                        .collect(Collectors.toMap(
                                v -> v.getVariable().getCode(),
                                ContractVariableValueEntity::getValue
                        ));
                log.info("Loaded {} variable values from repository for contract {}", contractVariablesMap.size(), contract.getPublicId());
            }
        }

        allVariablesMap.putAll(contractVariablesMap);
        
        String contentWithSignatures = contractPdfService.processSignaturePlaceholdersBeforeMerge(contract.getContent(), contract);
        
        String mergedContent = populateContent(contentWithSignatures, allVariablesMap);
        
        log.info("Firmas procesadas como imágenes base64 para vista previa HTML del contrato {}", contract.getPublicId());

        return new ContractContentDTO(
                contract.getPublicId(),
                contract.getContractNumber(),
                mergedContent,
                contract.getCreatedAt(),
                contract.getUpdatedAt()
        );
    }

    @Deprecated
    @Override
    @Transactional
    public UploadUrlResponse generateUploadUrl(UUID publicId, GenerateUploadUrlRequest request) {
        log.warn("generateUploadUrl está deprecado. Use el endpoint de archivos internos directamente.");
        throw new UnsupportedOperationException("Este método está deprecado. Use el endpoint de archivos internos: POST /v1/internal-files/upload");
    }

    @Deprecated
    @Override
    @Transactional
    public void attachFile(UUID publicId, AttachFileRequest request) {
        log.warn("attachFile está deprecado. Use uploadFile directamente.");
        throw new UnsupportedOperationException("Este método está deprecado. Use uploadFile directamente.");
    }

    @Override
    @Transactional
    public void uploadFile(UUID publicId, org.springframework.web.multipart.MultipartFile file, String description) {
        log.info("Subiendo archivo {} para contrato con publicId: {}", file.getOriginalFilename(), publicId);
        ContractEntity contract = findByPublicId(publicId);

        try {
            InternalFileEntity savedFile = internalFileStorageService.saveFile(
                    contract,
                    file,
                    FileCategory.CONTRACT.getCode(),
                    description != null ? description : "Archivo adjuntado al contrato " + contract.getContractNumber()
            );

            log.info("Archivo guardado exitosamente con publicId: {} para contrato {}", savedFile.getPublicId(), publicId);
        } catch (Exception e) {
            log.error("Error guardando archivo para contrato {}: {}", publicId, e.getMessage(), e);
            throw new RuntimeException("Error guardando archivo: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void signContract(UUID publicId) {
        log.info("Attempting to sign contract with publicId: {}", publicId);
        ContractEntity contract = findByPublicId(publicId);

        List<InternalFileEntity> contractFiles = internalFileStorageService.getFilesByFileableAndCategory(
                contract, FileCategory.CONTRACT.getCode());

        if (contractFiles.isEmpty()) {
            throw new BusinessValidationException("exception.hiring.contract.no-images-attached");
        }

        StateEntity signedState = stateService.findByCodeAndDomainName(ContractStateEnum.SIGNED.getCode(), ContractEntity.TABLE_NAME);
        contract.setState(signedState);
        contractRepository.save(contract);

        try {
            EmployeeEntity existingEmployee = employeeService.findByDocumentNumber(contract.getPersonDocumentNumber());
            boolean needsUpdate = false;
            
            if (!existingEmployee.getPosition().getId().equals(contract.getPosition().getId())) {
                existingEmployee.setPosition(contract.getPosition());
                needsUpdate = true;
                log.info("Updating employee {} position to {} when signing contract {}", 
                        contract.getPersonDocumentNumber(), 
                        contract.getPosition().getName(), 
                        publicId);
            }
            
            if (!existingEmployee.getSubsidiary().getId().equals(contract.getSubsidiary().getId())) {
                existingEmployee.setSubsidiary(contract.getSubsidiary());
                needsUpdate = true;
                log.info("Updating employee {} subsidiary to {} when signing contract {}", 
                        contract.getPersonDocumentNumber(), 
                        contract.getSubsidiary().getName(), 
                        publicId);
            }
            
            if (needsUpdate) {
                employeeRepository.save(existingEmployee);
                log.info("Updated employee {} with contract data when signing contract {}", 
                        contract.getPersonDocumentNumber(), 
                        publicId);
            } else {
                log.info("Employee {} already has correct position and subsidiary for contract {}", 
                        contract.getPersonDocumentNumber(), 
                        publicId);
            }
        } catch (IdentifierNotFoundException e) {
            CreateEmployeeRequest createEmployeeRequest = new CreateEmployeeRequest(
                    contract.getPersonDocumentNumber(),
                    contract.getSubsidiary().getPublicId(),
                    contract.getPosition().getPublicId(),
                    null, // Conceptos no disponibles en este punto
                    null
            );
            employeeService.create(createEmployeeRequest);
            log.warn("Employee created for contract {} when signing (should have been created when contract was created)", publicId);
        }

        log.info("Usuario {} activado para contrato {} en posición {}", 
            contract.getPersonDocumentNumber(), 
            publicId,
            contract.getPosition().getName());

        log.info("Contract {} signed successfully.", publicId);
    }

    @Override
    @Transactional
    public void signContract(UUID publicId, MultipartFile signatureFile) {
        log.info("Attempting to sign contract {} with employee signature", publicId);
        ContractEntity contract = findByPublicId(publicId);

        if (signatureFile == null || signatureFile.isEmpty()) {
            throw new BusinessValidationException("exception.hiring.contract.signature-required", 
                    "La firma del empleado es obligatoria");
        }

        String signatureUrl;
        try {
            log.info("Subiendo firma del empleado para contrato {}", publicId);
            Map<String, Object> uploadResult = fileStorageService.uploadFile(signatureFile, Bucket.SIGNATURE);
            signatureUrl = (String) uploadResult.get("secure_url");
            if (signatureUrl == null) {
                signatureUrl = (String) uploadResult.get("url");
            }
            log.info("Firma del empleado subida exitosamente. URL: {}", signatureUrl);
        } catch (Exception e) {
            log.error("Error subiendo firma del empleado para contrato {}: {}", publicId, e.getMessage(), e);
            throw new BusinessValidationException("exception.hiring.contract.signature-upload-error", 
                    "Error al subir la firma: " + e.getMessage());
        }

        ContractEntity contractWithVars = contractRepository.findById(contract.getEntityId())
                .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.contract.not-found", publicId.toString()));
        
        Map<String, String> currentVariables = new HashMap<>();
        if (contractWithVars.getVariableValues() != null) {
            currentVariables = contractWithVars.getVariableValues().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            vv -> vv.getVariable().getCode(),
                            ContractVariableValueEntity::getValue
                    ));
        }
        
        currentVariables.put("FIRMA_EMPLEADOR", signatureUrl);
        log.info("Variable FIRMA_EMPLEADOR {} con URL: {}", 
                currentVariables.containsKey("FIRMA_EMPLEADOR") ? "actualizada" : "agregada", 
                signatureUrl);
        
        contractVariableValueService.update(contractWithVars, currentVariables);

        StateEntity signedState = stateService.findByCodeAndDomainName(
                ContractStateEnum.SIGNED.getCode(), ContractEntity.TABLE_NAME);
        contract.setState(signedState);
        contractRepository.save(contract);

        try {
            EmployeeEntity existingEmployee = employeeService.findByDocumentNumber(contract.getPersonDocumentNumber());
            boolean needsUpdate = false;
            
            if (!existingEmployee.getPosition().getId().equals(contract.getPosition().getId())) {
                existingEmployee.setPosition(contract.getPosition());
                needsUpdate = true;
                log.info("Updating employee {} position to {} when signing contract {}", 
                        contract.getPersonDocumentNumber(), 
                        contract.getPosition().getName(), 
                        publicId);
            }
            
            // Actualizar subsidiaria si es diferente
            if (!existingEmployee.getSubsidiary().getId().equals(contract.getSubsidiary().getId())) {
                existingEmployee.setSubsidiary(contract.getSubsidiary());
                needsUpdate = true;
                log.info("Updating employee {} subsidiary to {} when signing contract {}", 
                        contract.getPersonDocumentNumber(), 
                        contract.getSubsidiary().getName(), 
                        publicId);
            }
            
            final UUID retirementConceptPublicId;
            final UUID healthInsuranceConceptPublicId;
            
            try {
                retirementConceptPublicId = currentVariables.containsKey("_INTERNAL_RETIREMENT_CONCEPT_PUBLIC_ID") 
                    ? UUID.fromString(currentVariables.get("_INTERNAL_RETIREMENT_CONCEPT_PUBLIC_ID"))
                    : null;
            } catch (IllegalArgumentException e) {
                log.warn("Invalid retirement concept public ID in contract {} variables: {}", 
                        publicId, currentVariables.get("_INTERNAL_RETIREMENT_CONCEPT_PUBLIC_ID"));
                throw new BusinessValidationException("exception.hiring.contract.invalid-retirement-concept-id");
            }
            
            try {
                healthInsuranceConceptPublicId = currentVariables.containsKey("_INTERNAL_HEALTH_INSURANCE_CONCEPT_PUBLIC_ID") 
                    ? UUID.fromString(currentVariables.get("_INTERNAL_HEALTH_INSURANCE_CONCEPT_PUBLIC_ID"))
                    : null;
            } catch (IllegalArgumentException e) {
                log.warn("Invalid health insurance concept public ID in contract {} variables: {}", 
                        publicId, currentVariables.get("_INTERNAL_HEALTH_INSURANCE_CONCEPT_PUBLIC_ID"));
                        throw new BusinessValidationException("exception.hiring.contract.invalid-health-insurance-concept-id");
            }
            
            if (retirementConceptPublicId != null) {
                ConceptEntity retirementConcept = conceptRepository.findByPublicId(retirementConceptPublicId)
                        .orElseThrow(() -> new IdentifierNotFoundException("exception.concept.not-found", retirementConceptPublicId.toString()));
                existingEmployee.setRetirementConcept(retirementConcept);
                needsUpdate = true;
                log.info("Updating employee {} retirement concept to {} when signing contract {}", 
                        contract.getPersonDocumentNumber(), 
                        retirementConceptPublicId, 
                        publicId);
            }
            
            if (healthInsuranceConceptPublicId != null) {
                ConceptEntity healthInsuranceConcept = conceptRepository.findByPublicId(healthInsuranceConceptPublicId)
                        .orElseThrow(() -> new IdentifierNotFoundException("exception.concept.not-found", healthInsuranceConceptPublicId.toString()));
                existingEmployee.setHealthInsuranceConcept(healthInsuranceConcept);
                needsUpdate = true;
                log.info("Updating employee {} health insurance concept to {} when signing contract {}", 
                        contract.getPersonDocumentNumber(), 
                        healthInsuranceConceptPublicId, 
                        publicId);
            }
            
            // Guardar empleado actualizado si hubo cambios
            if (needsUpdate) {
                employeeRepository.save(existingEmployee);
                log.info("Updated employee {} with contract data (position, subsidiary, concepts) when signing contract {}", 
                        contract.getPersonDocumentNumber(), 
                        publicId);
            } else {
                log.info("Employee {} already has correct data for contract {}", 
                        contract.getPersonDocumentNumber(), 
                        publicId);
            }
        } catch (IdentifierNotFoundException e) {
            UUID retirementConceptPublicId = null;
            UUID healthInsuranceConceptPublicId = null;
            
            if (currentVariables.containsKey("_INTERNAL_RETIREMENT_CONCEPT_PUBLIC_ID")) {
                try {
                    retirementConceptPublicId = UUID.fromString(currentVariables.get("_INTERNAL_RETIREMENT_CONCEPT_PUBLIC_ID"));
                } catch (IllegalArgumentException ex) {
                    log.warn("Invalid retirement concept public ID in contract {} variables", publicId);
                }
            }
            
            if (currentVariables.containsKey("_INTERNAL_HEALTH_INSURANCE_CONCEPT_PUBLIC_ID")) {
                try {
                    healthInsuranceConceptPublicId = UUID.fromString(currentVariables.get("_INTERNAL_HEALTH_INSURANCE_CONCEPT_PUBLIC_ID"));
                } catch (IllegalArgumentException ex) {
                    log.warn("Invalid health insurance concept public ID in contract {} variables", publicId);
                }
            }
            
            CreateEmployeeRequest createEmployeeRequest = new CreateEmployeeRequest(
                    contract.getPersonDocumentNumber(),
                    contract.getSubsidiary().getPublicId(),
                    contract.getPosition().getPublicId(),
                    retirementConceptPublicId,
                    healthInsuranceConceptPublicId
            );
            employeeService.create(createEmployeeRequest);
            log.info("Created employee {} with contract data when signing contract {}", 
                    contract.getPersonDocumentNumber(), 
                    publicId);
        }

        ensureUserExistsForEmployee(contract.getPersonDocumentNumber());

        try {
            contractRepository.flush();
            
            ContractEntity contractWithUpdatedVars = contractRepository.findById(contract.getEntityId())
                    .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.contract.not-found", publicId.toString()));
            
            ContractContentDTO contractContent = getContractContent(publicId);
            String htmlContent = contractContent.mergedContent();
            
            contractPdfService.generateAndSaveContractPdf(contractWithUpdatedVars, htmlContent, true);
            log.info("PDF generated and saved for contract {}", publicId);
        } catch (Exception e) {
            log.error("Error generating PDF for contract {}: {}", publicId, e.getMessage(), e);
        }

        log.info("Contract {} signed successfully with employee signature.", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContractSearchDTO> searchByPersonDocumentNumber(String personDocumentNumber) {
        log.info("Searching contract by person document number: {}", personDocumentNumber);

        Optional<ContractEntity> contractOpt = contractRepository.findByPersonDocumentNumber(personDocumentNumber);
        if (contractOpt.isEmpty()) {
            log.info("Contract with person document number {} not found", personDocumentNumber);
            return Optional.empty();
        }

        ContractEntity contract = contractOpt.get();

        List<ImageEntity> images = imageService.getImagesByImageable(contract);
        List<ContractSearchDTO.ContractImageDTO> imageUrls = images.stream()
                .map(img -> new ContractSearchDTO.ContractImageDTO(img.getPublicId(), img.getUrl(), img.getOrderValue()))
                .toList();

        ContractSearchDTO searchResult = contractMapper.toSearchDTO(contract, imageUrls);

        PersonEntity person = personAPI.findOrCreatePersonByDni(personDocumentNumber);
        ContractSearchDTO finalResult = new ContractSearchDTO(
                searchResult.publicId(),
                searchResult.contractNumber(),
                searchResult.startDate(),
                searchResult.endDate(),
                searchResult.personDocumentNumber(),
                person.getNames() + " " + person.getPaternalLastname()+ " "+person.getMaternalLastname(),
                searchResult.contractTypeName(),
                searchResult.stateName(),
                searchResult.imageUrls()
        );

        log.info("Contract found for person: {}", personDocumentNumber);
        return Optional.of(finalResult);
    }

    @Override
    @Transactional
    public void cancelContract(UUID publicId) {
        log.info("Attempting to cancel contract with publicId: {}", publicId);
        ContractEntity contract = findByPublicId(publicId);

        StateEntity canceledState = stateService.findByCodeAndDomainName(ContractStateEnum.CANCELLED.getCode(), ContractEntity.TABLE_NAME);
        contract.setState(canceledState);
        contractRepository.save(contract);

        log.info("Usuario {} desactivado para contrato {} en posición {}", 
            contract.getPersonDocumentNumber(), 
            publicId,
            contract.getPosition().getName());

        log.info("Contract {} canceled successfully.", publicId);
    }

    @Override
    @Transactional
    public void updateExtendedEndDate(UUID contractPublicId, LocalDate newEndDate) {
        log.info("Updating extended end date for contract {} to {}", contractPublicId, newEndDate);

        ContractEntity contract = findByPublicId(contractPublicId);
        contract.setExtendedEndDate(newEndDate);

        contractRepository.save(contract);
        log.info("Successfully updated extended end date for contract {}", contractPublicId);
    }


    public ContractEntity findByPublicId(UUID publicId) {
        return contractRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.contract.not-found", publicId));
    }

    private String generateContractNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = String.format("%04d", new SecureRandom().nextInt(10000));
        return "CONTR_" + datePart + "_" + randomPart;
    }

    private void validateRequiredNonDefaultVariables(List<String> templateVariables, Set<String> providedVariableCodes) {
        List<String> missingRequiredVariables = templateVariables.stream()
                .filter(variable -> !isDefaultSystemVariable(variable.toUpperCase()))
                .filter(variable -> !providedVariableCodes.contains(variable))
                .toList();

        if (!missingRequiredVariables.isEmpty()) {
            String missingVarsString = String.join(", ", missingRequiredVariables);
            throw new DataIntegrityViolationException(
                    "exception.hiring.missing-required-variables",
                    new Object[]{missingVarsString}
            );
        }
    }

    private Map<String, String> combineVariablesForStorage(List<String> templateVariables,
                                                          Map<String, String> providedValues,
                                                          ContractEntity contract) {
        Map<String, String> allVariables = new HashMap<>(providedValues);

        for (String variableCode : templateVariables) {
            if (isDefaultSystemVariable(variableCode.toUpperCase()) && !allVariables.containsKey(variableCode)) {
                String defaultValue = getDefaultVariableValue(variableCode, contract);
                if (defaultValue != null) {
                    allVariables.put(variableCode, defaultValue);
                }
            }
        }

        return allVariables;
    }

    private boolean isDefaultSystemVariable(String code) {
        return code.equals("COMPANY_NAME") ||
               code.equals("COMPANY_RUC") ||
               code.equals("COMPANY_ADDRESS") ||
               code.equals("CONTRACT_DATE") ||
               code.equals("CONTRACT_NUMBER") ||
               code.equals("CURRENT_DATE") ||
               code.equals("SYSTEM_USER") ||
               code.equals("FIRMA_EMPLEADOR") ||
               code.equals("FIRMA_ENCARGADO") ||
               code.startsWith("AUTO_") ||
               code.startsWith("SYSTEM_");
    }

    private String getDefaultVariableValue(String variableCode, ContractEntity contract) {
        return switch (variableCode.toUpperCase()) {
            case "COMPANY_NAME" -> getCurrentCompanyName();
            case "COMPANY_RUC" -> getCurrentCompanyRuc();
            case "COMPANY_ADDRESS" -> getCurrentCompanyAddress();
            case "CONTRACT_DATE" -> contract.getStartDate().toString();
            case "CONTRACT_NUMBER" -> contract.getContractNumber();
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

    private String getCurrentCompanyName() {
        return "EMPRESA EJEMPLO S.A.C.";
    }

    private String getCurrentCompanyRuc() {
        return "20123456789";
    }

    private String getCurrentCompanyAddress() {
        return "Av. Ejemplo 123, Lima, Perú";
    }

    private String getCurrentDate() {
        return LocalDate.now().toString();
    }

    private String getCurrentUser() {
        return "Sistema";
    }

    private String getSystemGeneratedValue(String variableCode) {
        return "[VALOR_GENERADO]";
    }

    private String populateContent(String templateContent, Map<String, String> providedValues) {
        String result = templateContent;
        for (Map.Entry<String, String> entry : providedValues.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }

    private boolean isUpdateRedundant(UpdateContractRequest request, ContractEntity entity) {
        Map<String, String> currentVariables = entity.getVariableValues().stream()
                .collect(Collectors.toMap(v -> v.getVariable().getCode(), ContractVariableValueEntity::getValue));

        Map<String, String> newVariables = request.variables().stream()
                .collect(Collectors.toMap(ContractVariableValuePayload::code, ContractVariableValuePayload::value));

        return Objects.equals(request.contractTypePublicId(), entity.getContractType().getPublicId()) &&
                Objects.equals(request.statePublicId(), entity.getState().getPublicId()) &&
                Objects.equals(request.subsidiaryPublicId(), entity.getSubsidiary().getPublicId()) &&
                Objects.equals(request.positionPublicId(), entity.getPosition().getPublicId()) &&
                Objects.equals(request.templatePublicId(), entity.getTemplate().getPublicId()) &&
                Objects.equals(currentVariables, newVariables);
    }

    private LocalDate validateContractTypeAndEndDate(ContractTypeEntity contractType, LocalDate requestEndDate) {
        String contractTypeCode = contractType.getCode();

        if (ContractTypeEnum.INDEFINIDO.getCode().equals(contractTypeCode)) {
            return null;
        } else if (ContractTypeEnum.PLAZO.getCode().equals(contractTypeCode)) {
            if (requestEndDate == null) {
                throw new BusinessValidationException("exception.hiring.contract.end-date-required-for-fixed-term");
            }
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(currentDate, requestEndDate);

            if (daysBetween < MIN_CONTRACT_DURATION_DAYS) {
                throw new BusinessValidationException(
                    "exception.hiring.contract.duration-too-short",
                    new Object[]{MIN_CONTRACT_DURATION_DAYS}
                );
            }

            return requestEndDate;
        } else {
            return requestEndDate;
        }
    }

    private void validateSubsidiarySigner(SubsidiaryEntity subsidiary) {
        if (subsidiary == null) {
            throw new BusinessValidationException(
                "exception.hiring.contract.missing-subsidiary"
            );
        }

        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();
        Short subsidiaryId = subsidiary.getId();

        Optional<CompanySubsidiarySignerEntity> signerOpt = companySubsidiarySignerRepository
            .findLatestByCompanyAndSubsidiary(company.getId(), subsidiaryId);

        if (signerOpt.isEmpty()) {
            signerOpt = companySubsidiarySignerRepository.findLatestByCompany(company.getId());
        }

        if (signerOpt.isEmpty()) {
            throw new BusinessValidationException(
                "exception.hiring.contract.missing-signer",
                subsidiary.getName()
            );
        }

        CompanySubsidiarySignerEntity signer = signerOpt.get();
        
        String signatureUrl = getSubsidiarySignerSignatureUrl(subsidiary);
        if (signatureUrl == null || signatureUrl.trim().isEmpty()) {
            throw new BusinessValidationException(
                "exception.hiring.contract.missing-signature-image",
                subsidiary.getName()
            );
        }

        log.info("Responsable de firma validado para subsidiaria {}: {} (con imagen de firma: {})", 
            subsidiary.getName(), 
            signer.getResponsibleEmployee().getPersonDocumentNumber(),
            signatureUrl            );
    }

    private String getSubsidiarySignerSignatureUrl(SubsidiaryEntity subsidiary) {
        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();
        Short subsidiaryId = subsidiary.getId();

        Optional<CompanySubsidiarySignerEntity> signerOpt = companySubsidiarySignerRepository
            .findLatestByCompanyAndSubsidiary(company.getId(), subsidiaryId);

        if (signerOpt.isEmpty()) {
            signerOpt = companySubsidiarySignerRepository.findLatestByCompany(company.getId());
        }

        if (signerOpt.isEmpty()) {
            return null;
        }

        CompanySubsidiarySignerEntity signer = signerOpt.get();
        
        try {
            List<InternalFileEntity> files = internalFileStorageService.getFilesByFileableAndCategory(
                signer, FileCategory.SIGNATURE.getCode());
            if (!files.isEmpty()) {
                InternalFileEntity file = files.get(0);
                return "/v1/internal-files/" + file.getPublicId() + "/download";
            }
        } catch (Exception e) {
            log.debug("No se encontró archivo interno de firma para signer {}", signer.getPublicId());
        }
        
        return signer.getSignatureImageUrl();
    }

    @Transactional
    private void ensureUserExistsForEmployee(String documentNumber) {
        log.info("Ensuring user exists for employee with document number: {}", documentNumber);

        Optional<UserEntity> existingUserOpt = userRepository.findByEmployeeId(documentNumber);
        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();
            if (!existingUser.getIsActive()) {
                log.info("User for employee {} exists but is disabled. Reactivating user.", documentNumber);
                existingUser.setIsActive(true);
                userRepository.save(existingUser);
                log.info("User reactivated for employee {}", documentNumber);
            } else {
                log.info("User already exists and is active for employee {}. No action needed.", documentNumber);
            }
            return;
        }

        Optional<UserEntity> userByUsernameOpt = userRepository.findByUsername(documentNumber);
        if (userByUsernameOpt.isPresent()) {
            UserEntity existingUser = userByUsernameOpt.get();
            if (existingUser.getEmployeeId() == null || !existingUser.getEmployeeId().equals(documentNumber)) {
                existingUser.setEmployeeId(documentNumber);
                if (!existingUser.getIsActive()) {
                    existingUser.setIsActive(true);
                }
                userRepository.save(existingUser);
                log.info("Updated existing user {} to link with employee {}", existingUser.getUsername(), documentNumber);
            }
            return;
        }

        if (userRepository.existsByUsername(documentNumber)) {
            log.warn("User with username {} exists but was not found in previous queries. Reloading from database.", documentNumber);
            Optional<UserEntity> reloadedUser = userRepository.findByUsername(documentNumber);
            if (reloadedUser.isPresent()) {
                UserEntity existingUser = reloadedUser.get();
                if (existingUser.getEmployeeId() == null || !existingUser.getEmployeeId().equals(documentNumber)) {
                    existingUser.setEmployeeId(documentNumber);
                }
                if (!existingUser.getIsActive()) {
                    existingUser.setIsActive(true);
                }
                userRepository.save(existingUser);
                log.info("Reloaded and updated existing user {} for employee {}", existingUser.getUsername(), documentNumber);
                return;
            }
        }

        log.info("User does not exist for employee {}. Creating new user with base profile '{}'.", 
                documentNumber, BaseProfileEnum.COLABORADOR.getName());

        ProfileEntity colaboradorProfile = profileRepository.findByName(BaseProfileEnum.COLABORADOR.getName())
                .orElseThrow(() -> new BusinessValidationException("exception.auth.profile.colaborador-not-found", 
                        String.format("El perfil base '%s' no existe en el sistema. Por favor, asegúrese de que la migración V116 se haya ejecutado correctamente.", 
                                BaseProfileEnum.COLABORADOR.getName())));

        try {
            UserEntity newUser = new UserEntity();
            newUser.setEmployeeId(documentNumber);
            newUser.setUsername(documentNumber);
            String defaultPassword = documentNumber;
            String passwordHash = passwordEncoder.encode(defaultPassword);
            newUser.setPasswordHash(passwordHash);
            newUser.setProfileId(colaboradorProfile.getId());
            newUser.setIsActive(true);

            UserEntity savedUser = userRepository.save(newUser);
            log.info("User created for employee {} with username: {} and profile: {}", 
                    documentNumber, savedUser.getUsername(), colaboradorProfile.getName());

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

            log.info("User setup completed for employee {}. The user can now log in with username: {} and password: {} (default password is the document number)", 
                    documentNumber, savedUser.getUsername(), defaultPassword);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("User creation failed due to constraint violation (likely concurrent creation). Reloading existing user for employee {}", documentNumber);
            
            Optional<UserEntity> concurrentUser = userRepository.findByUsername(documentNumber);
            if (concurrentUser.isPresent()) {
                UserEntity existingUser = concurrentUser.get();
                if (existingUser.getEmployeeId() == null || !existingUser.getEmployeeId().equals(documentNumber)) {
                    existingUser.setEmployeeId(documentNumber);
                }
                if (!existingUser.getIsActive()) {
                    existingUser.setIsActive(true);
                }
                userRepository.save(existingUser);
                log.info("Reloaded and updated concurrently created user {} for employee {}", existingUser.getUsername(), documentNumber);
            } else {
                log.error("Failed to create user for employee {} and could not reload it. Original error: {}", documentNumber, e.getMessage());
                throw e;
            }
        }
    }
}
