package com.agropay.core.auth.application.usecase;

import com.agropay.core.auth.model.profile.*;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IProfileUseCase {
    CommandProfileResponse create(CreateProfileRequest request);
    CommandProfileResponse update(UUID publicId, UpdateProfileRequest request);
    void delete(UUID publicId);
    ProfileDetailsDTO getByPublicId(UUID publicId);
    CommandProfileResponse getCommandResponseByPublicId(UUID publicId);
    PagedResult<ProfileListDTO> findAllPaged(String query, Pageable pageable);
    void assignElements(UUID profilePublicId, AssignElementsRequest request);
    ProfileElementsByContainerDTO getElementsByContainer(UUID profilePublicId);
}

