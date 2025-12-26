package com.agropay.core.auth.application.usecase;

import com.agropay.core.auth.model.container.*;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IContainerUseCase {
    CommandContainerResponse create(CreateContainerRequest request);
    CommandContainerResponse update(UUID publicId, UpdateContainerRequest request);
    void delete(UUID publicId);
    ContainerDetailsDTO getByPublicId(UUID publicId);
    CommandContainerResponse getCommandResponseByPublicId(UUID publicId);
    PagedResult<ContainerListDTO> findAllPaged(String query, Pageable pageable);
}

