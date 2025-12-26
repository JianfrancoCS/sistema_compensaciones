package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.domain.AddendumTemplateEntity;
import com.agropay.core.hiring.model.addendumtemplate.*;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IAddendumTemplateUseCase {
    CommandAddendumTemplateResponse create(CreateAddendumTemplateRequest request);

    CommandAddendumTemplateResponse update(UUID publicId, UpdateAddendumTemplateRequest request);

    void delete(UUID publicId);

    CommandAddendumTemplateResponse getCommandResponseByPublicId(UUID publicId);

    AddendumTemplateDetailsDTO getDetailsByPublicId(UUID publicId);

    PagedResult<AddendumTemplateListDTO> findAllPaged(String code, String name, UUID addendumTypePublicId, Pageable pageable);

    List<AddendumTemplateSelectOptionDTO> getSelectOptions(UUID addendumTypePublicId);

    AddendumTemplateEntity findByPublicId(UUID publicId);

    List<StateSelectOptionDTO> getStatesSelectOptions();
}