package com.agropay.core.attendance.application.usecase;

import com.agropay.core.attendance.domain.MarkingReasonEntity;
import com.agropay.core.attendance.model.markingreason.*;
import com.agropay.core.shared.utils.PagedResult;

import java.util.List;
import java.util.UUID;

public interface IMarkingReasonUseCase {

    PagedResult<MarkingReasonListDTO> findAllPaged(MarkingReasonPageableRequest request);

    List<MarkingReasonSelectOptionDTO> getSelectOptions();

    List<MarkingReasonSelectOptionDTO> getExternalSelectOptions();

    List<MarkingReasonSelectOptionDTO> getInternalMarkingReasons();

    List<MarkingReasonSelectOptionDTO> getExternalMarkingReasons();

    MarkingReasonEntity findByPublicId(UUID publicId);

    MarkingReasonEntity findByCode(String code);
}