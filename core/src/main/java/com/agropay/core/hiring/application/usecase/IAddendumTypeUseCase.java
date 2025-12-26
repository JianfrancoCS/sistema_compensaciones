package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.domain.AddendumTypeEntity;
import com.agropay.core.hiring.model.addendumtype.AddendumTypeSelectOptionDTO;

import java.util.List;
import java.util.UUID;

public interface IAddendumTypeUseCase {
    List<AddendumTypeSelectOptionDTO> getSelectOptions();

    AddendumTypeEntity findByPublicId(UUID publicId);
}