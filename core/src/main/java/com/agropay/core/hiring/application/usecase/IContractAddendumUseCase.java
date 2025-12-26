package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.domain.AddendumEntity;
import com.agropay.core.hiring.model.addendum.*;
import com.agropay.core.shared.generic.usecase.IGetAllPaged;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IContractAddendumUseCase  {
    CommandAddendumResponse create(CreateAddendumRequest request);

    CommandAddendumResponse update(UUID publicId, UpdateAddendumRequest request);

    void delete(UUID publicId);

    AddendumDetailsDTO getDetailsByPublicId(UUID publicId);

    CommandAddendumResponse getCommandResponseByPublicId(UUID publicId);

    PagedResult<AddendumListDTO> findAllPaged(String addendumNumber, UUID contractPublicId, Pageable pageable);

    List<AddendumListDTO> getByContractPublicId(UUID contractPublicId);

    AddendumContentDTO getContentByPublicId(UUID publicId);

    CommandAddendumResponse signAddendum(UUID publicId, SignAddendumRequest request);

    UploadUrlResponse generateUploadUrl(GenerateUploadUrlRequest request);

    CommandAddendumResponse attachFile(UUID publicId, AttachFileRequest request);

    List<StateSelectOptionDTO> getStatesSelectOptions();

    // Method for internal service communication
    AddendumEntity findByPublicId(UUID publicId);
}