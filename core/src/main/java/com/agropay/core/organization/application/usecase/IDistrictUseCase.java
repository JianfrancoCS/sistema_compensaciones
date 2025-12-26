package com.agropay.core.organization.application.usecase;


import com.agropay.core.organization.domain.DistrictEntity;
import com.agropay.core.organization.model.location.DistrictDetailResponseDTO;
import com.agropay.core.shared.generic.usecase.IGetAllByIdentifier;
import com.agropay.core.shared.generic.usecase.IGetByIdentifier;

import java.util.UUID;

public interface IDistrictUseCase extends
        IGetAllByIdentifier<DistrictEntity, UUID>,
        IGetByIdentifier<DistrictEntity,UUID>{
    DistrictEntity findByPublicId(UUID publicId);

    DistrictDetailResponseDTO getDetailByPublicId(UUID id);
}
