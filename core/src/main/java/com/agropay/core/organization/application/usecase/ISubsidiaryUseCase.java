package com.agropay.core.organization.application.usecase;

import com.agropay.core.organization.api.ISubsidiaryAPI;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.organization.model.subsidiary.*;
import com.agropay.core.shared.generic.usecase.IGetAllPaged;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ISubsidiaryUseCase extends IGetAllPaged<SubsidiaryListDTO>, ISubsidiaryAPI {

    CommandSubsidiaryResponse create(CreateSubsidiaryRequest request);

    CommandSubsidiaryResponse update(UUID publicId, UpdateSubsidiaryRequest request);

    void delete(UUID publicId);

    SubsidiaryDetailsDTO getByPublicId(UUID publicId);

    PagedResult<SubsidiaryListDTO> findAllPaged(String name, Pageable pageable);

    List<SubsidiarySelectOptionDTO> getSelectOptions();

    List<SubsidiarySyncResponse> findAllForSync();

}
