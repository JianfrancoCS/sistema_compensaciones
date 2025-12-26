package com.agropay.core.assignment.application.usecase;

import com.agropay.core.assignment.domain.LaborUnitEntity;
import com.agropay.core.assignment.model.laborunit.*;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ILaborUnitUseCase {

    CommandLaborUnitResponse create(CreateLaborUnitRequest request);

    CommandLaborUnitResponse update(UUID publicId, UpdateLaborUnitRequest request);

    void delete(UUID publicId);

    List<LaborUnitSelectOptionDTO> getSelectOptions();

    PagedResult<LaborUnitListDTO> findAllPaged(String name, String abbreviation, Pageable pageable);

    LaborUnitEntity findByPublicId(UUID publicId);
}