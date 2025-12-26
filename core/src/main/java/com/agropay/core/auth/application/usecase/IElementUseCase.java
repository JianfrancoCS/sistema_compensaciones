package com.agropay.core.auth.application.usecase;

import com.agropay.core.auth.model.element.*;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IElementUseCase {
    CommandElementResponse create(CreateElementRequest request);
    CommandElementResponse update(UUID publicId, UpdateElementRequest request);
    void delete(UUID publicId);
    ElementDetailsDTO getByPublicId(UUID publicId);
    CommandElementResponse getCommandResponseByPublicId(UUID publicId);
    PagedResult<ElementListDTO> findAllPaged(String query, UUID containerPublicId, Pageable pageable);
}

