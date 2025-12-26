package com.agropay.core.assignment.application.usecase;

import com.agropay.core.assignment.domain.LaborEntity;
import com.agropay.core.assignment.model.labor.*;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ILaborUseCase {

    CommandLaborResponse create(CreateLaborRequest request);

    CommandLaborResponse update(UUID publicId, UpdateLaborRequest request);

    void delete(UUID publicId);

    List<LaborSelectOptionDTO> getSelectOptions();

    PagedResult<LaborListDTO> findAllPaged(String name, Boolean isPiecework, UUID laborUnitPublicId, Pageable pageable);

    LaborEntity findByPublicId(UUID publicId);

    List<LaborSyncResponse> findAllForSync();
}