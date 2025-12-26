package com.agropay.core.organization.application.usecase;

import com.agropay.core.organization.domain.AreaEntity;
import com.agropay.core.organization.model.area.*;
import com.agropay.core.shared.generic.usecase.IGetAllPaged;

import java.util.List;
import java.util.UUID;

public interface IAreaUseCase extends IGetAllPaged<AreaListDTO> {

    CommandAreaResponse create(CreateAreaRequest request);

    CommandAreaResponse update(UUID publicId, UpdateAreaRequest request);

    void delete(UUID publicId);

    AreaDetailsDTO getByPublicId(UUID publicId);

    List<AreaSelectOptionDTO> getSelectOptions();

    AreaEntity findByPublicId(UUID publicId);
}
