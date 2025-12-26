package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.domain.ContractTemplateEntity;
import com.agropay.core.hiring.model.contracttemplate.*;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IContractTemplateUseCase {
    CommandContractTemplateResponse create(CreateContractTemplateRequest request);

    CommandContractTemplateResponse update(UUID publicId, UpdateContractTemplateRequest request);

    void delete(UUID publicId);


    CommandContractTemplateResponse getCommandResponseByPublicId(UUID publicId);

    ContractTemplateContentDTO getContentByPublicId(UUID publicId);

    PagedResult<ContractTemplateListDTO> findAllPaged(String code, String name, UUID contractTypePublicId, UUID statePublicId, Pageable pageable);

    List<ContractTemplateSelectOptionDTO> getSelectOptions(UUID contractTypePublicId);

    ContractTemplateEntity findByPublicId(UUID publicId);

    List<StateSelectOptionDTO> getStatesSelectOptions();

    List<ContractTemplateVariableWithValidationDTO> getVariablesWithValidationByPublicId(UUID publicId);
}
