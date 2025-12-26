package com.agropay.core.organization.application.usecase;

import com.agropay.core.organization.api.IPositionAPI;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.model.position.*;
import com.agropay.core.shared.generic.usecase.IGetAllPaged;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPositionUseCase extends  IPositionAPI {

    CommandPositionResponse create(CreatePositionRequest request);

    CommandPositionResponse update(UUID publicId, UpdatePositionRequest request);

    void delete(UUID publicId);

    PositionDetailsDTO getByPublicId(UUID publicId);

    CommandPositionResponse getCommandResponseByPublicId(UUID publicId);

    List<PositionSelectOptionDTO> getSelectOptions(UUID areaPublicId);

    List<PositionSelectOptionDTO> getManagerSelectOptions();

    PagedResult<PositionListDTO> findAllPaged(String code, String name, UUID areaPublicId, Pageable pageable);

    List<PositionSyncResponse> findAllForSync();

}
