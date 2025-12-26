package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.application.usecase.IAddendumTemplateUseCase;
import com.agropay.core.hiring.application.usecase.IAddendumTypeUseCase;
import com.agropay.core.hiring.domain.AddendumTemplateEntity;
import com.agropay.core.hiring.domain.AddendumTemplateVariableEntity;
import com.agropay.core.hiring.domain.AddendumTypeEntity;
import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.hiring.mapper.IAddendumTemplateMapper;
import com.agropay.core.hiring.model.addendumtemplate.*;
import com.agropay.core.hiring.persistence.AddendumTemplateSpecification;
import com.agropay.core.hiring.persistence.IAddendumTemplateRepository;
import com.agropay.core.hiring.persistence.IAddendumTemplateVariableRepository;
import com.agropay.core.hiring.persistence.IVariableRepository;
import com.agropay.core.hiring.persistence.IContractAddendumRepository; // Import the repository
import com.agropay.core.states.application.IStateUseCase;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddendumTemplateServiceImpl implements IAddendumTemplateUseCase {
    private final String deletedBy = "anonymous";

    private final IAddendumTemplateRepository addendumTemplateRepository;
    private final IAddendumTemplateVariableRepository addendumTemplateVariableRepository;
    private final IAddendumTypeUseCase addendumTypeService;
    private final IStateUseCase stateService;
    private final IVariableRepository variableRepository;
    private final IAddendumTemplateMapper addendumTemplateMapper;
    private final IContractAddendumRepository contractAddendumRepository;

    @Override
    @Transactional
    public CommandAddendumTemplateResponse create(CreateAddendumTemplateRequest request) {
        log.info("Attempting to create a new addendum template with name: {}", request.name());
        if (addendumTemplateRepository.existsByName(request.name())) {
            throw new UniqueValidationException("exception.hiring.addendum-template.name-unique", request.name());
        }

        AddendumTypeEntity addendumType = addendumTypeService.findByPublicId(request.addendumTypePublicId());
        StateEntity state = stateService.findByPublicId(request.statePublicId());

        AddendumTemplateEntity template = addendumTemplateMapper.toEntity(request);
        template.setPublicId(UUID.randomUUID());
        template.setAddendumType(addendumType);
        template.setState(state);

        String generatedCode = "ATMP_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) +
                "_" + String.format("%04d", new SecureRandom().nextInt(10000));
        template.setCode(generatedCode);

        handleVariableAssociations(template, request.variables());

        AddendumTemplateEntity savedTemplate = addendumTemplateRepository.save(template);
        log.info("Successfully created addendum template with publicId: {}", savedTemplate.getPublicId());
        return addendumTemplateMapper.toCommandResponse(savedTemplate);
    }

    @Override
    @Transactional
    public CommandAddendumTemplateResponse update(UUID publicId, UpdateAddendumTemplateRequest request) {
        log.info("Attempting to update addendum template with publicId: {}", publicId);
        AddendumTemplateEntity template = findByPublicId(publicId);

        if (isUpdateRedundant(request, template)) {
            throw new NoChangesDetectedException("exception.shared.no-changes-detected");
        }

        if (request.name() != null && !request.name().equals(template.getName())) {
            addendumTemplateRepository.findByName(request.name()).ifPresent(existing -> {
                throw new UniqueValidationException("exception.hiring.addendum-template.name-unique", request.name());
            });
        }

        addendumTemplateMapper.updateEntityFromRequest(request, template);

        if (request.addendumTypePublicId() != null && !request.addendumTypePublicId().equals(template.getAddendumType().getPublicId())) {
            AddendumTypeEntity newAddendumType = addendumTypeService.findByPublicId(request.addendumTypePublicId());
            template.setAddendumType(newAddendumType);
        }

        if (request.statePublicId() != null && !request.statePublicId().equals(template.getState().getPublicId())) {
            StateEntity newState = stateService.findByPublicId(request.statePublicId());
            template.setState(newState);
        }

        handleVariableAssociations(template, request.variables());

        AddendumTemplateEntity updatedTemplate = addendumTemplateRepository.save(template);
        log.info("Successfully updated addendum template with publicId: {}", updatedTemplate.getPublicId());
        return addendumTemplateMapper.toCommandResponse(updatedTemplate);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete addendum template with publicId: {}", publicId);
        AddendumTemplateEntity template = findByPublicId(publicId);

        if (contractAddendumRepository.existsByTemplate_Id(template.getId())) {
            throw new ReferentialIntegrityException("exception.hiring.addendum-template.has-associated-addendums", template.getName());
        }

        List<AddendumTemplateVariableEntity> variablesToDelete = new ArrayList<>(template.getVariables());

        variablesToDelete.forEach(variable ->
                addendumTemplateVariableRepository.softDelete(variable.getId(), deletedBy));

        addendumTemplateRepository.softDelete(template.getId(), deletedBy);

        log.info("Successfully deleted addendum template with publicId: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandAddendumTemplateResponse getCommandResponseByPublicId(UUID publicId) {
        log.info("Fetching command response for addendum template with publicId: {}", publicId);
        return addendumTemplateMapper.toCommandResponse(findByPublicId(publicId));
    }

    @Override
    @Transactional(readOnly = true)
    public AddendumTemplateDetailsDTO getDetailsByPublicId(UUID publicId) {
        log.info("Fetching details for addendum template with publicId: {}", publicId);
        return addendumTemplateMapper.toDetailsDTO(findByPublicId(publicId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<AddendumTemplateListDTO> findAllPaged(String code, String name, UUID addendumTypePublicId, Pageable pageable) {
        log.info("Fetching paged list of addendum templates with code: {}, name: {}, addendumTypePublicId: {} and pageable: {}", code, name, addendumTypePublicId, pageable);
        Specification<AddendumTemplateEntity> spec = AddendumTemplateSpecification.filterBy(code, name, addendumTypePublicId);
        Page<AddendumTemplateEntity> templatePage = addendumTemplateRepository.findAll(spec, pageable);
        return addendumTemplateMapper.toPagedDTO(templatePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddendumTemplateSelectOptionDTO> getSelectOptions(UUID addendumTypePublicId) {
        log.info("Fetching select options for addendum templates");
        List<AddendumTemplateEntity> templates = addendumTemplateRepository.findAllByAddendumTypePublicId(addendumTypePublicId);
        return addendumTemplateMapper.toSelectOptionDTOs(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StateSelectOptionDTO> getStatesSelectOptions() {
        log.info("Fetching state select options for addendum templates");
        return stateService.findStateOptionsByDomainName(AddendumTemplateEntity.TABLE_NAME);
    }

    @Transactional(readOnly = true)
    public AddendumTemplateEntity findByPublicId(UUID publicId) {
        return addendumTemplateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.addendum-template.not-found", publicId));
    }

    private void handleVariableAssociations(AddendumTemplateEntity template, List<AddendumTemplateVariableRequest> variableRequests) {
        if (variableRequests != null) {
            template.getVariables().clear();
            if (!variableRequests.isEmpty()) {
                Set<AddendumTemplateVariableEntity> newVariables = buildTemplateVariables(template, variableRequests);
                template.getVariables().addAll(newVariables);
            }
        }
    }

    private boolean isUpdateRedundant(UpdateAddendumTemplateRequest request, AddendumTemplateEntity entity) {
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
                Objects.equals(request.addendumTypePublicId(), entity.getAddendumType().getPublicId()) &&
                Objects.equals(request.statePublicId(), entity.getState().getPublicId()) &&
                variablesAreRedundant;
    }

    private Set<AddendumTemplateVariableEntity> buildTemplateVariables(AddendumTemplateEntity template, List<AddendumTemplateVariableRequest> variableRequests) {
        if (variableRequests == null || variableRequests.isEmpty()) {
            return Collections.emptySet();
        }
        return variableRequests.stream()
                .map(varRequest -> {
                    VariableEntity variable = variableRepository.findByPublicId(varRequest.variablePublicId())
                            .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.variable.not-found", varRequest.variablePublicId()));
                    return AddendumTemplateVariableEntity.builder()
                            .addendumTemplate(template)
                            .variable(variable)
                            .isRequired(varRequest.isRequired())
                            .displayOrder(varRequest.displayOrder())
                            .build();
                })
                .collect(Collectors.toSet());
    }
}