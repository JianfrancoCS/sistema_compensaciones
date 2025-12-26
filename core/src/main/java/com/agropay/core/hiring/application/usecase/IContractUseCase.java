package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.api.IContractAPI;
import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.hiring.model.contract.*;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IContractUseCase extends IContractAPI {
    CommandContractResponse create(CreateContractRequest request);
    
    CommandContractResponse create(CreateContractRequest request, org.springframework.web.multipart.MultipartFile photo);

    CommandContractResponse update(UUID publicId, UpdateContractRequest request);

    ContractDetailsDTO getDetailsByPublicId(UUID publicId);

    CommandContractResponse getCommandResponseByPublicId(UUID publicId);

    PagedResult<ContractListDTO> findAllPaged(
            String contractNumber, String personDocumentNumber,
            UUID contractTypePublicId, UUID statePublicId, Pageable pageable
    );

    List<StateSelectOptionDTO> getStatesSelectOptions();

    ContractContentDTO getContractContent(UUID publicId);

    UploadUrlResponse generateUploadUrl(UUID publicId, GenerateUploadUrlRequest request);

    void attachFile(UUID publicId, AttachFileRequest request);

    void uploadFile(UUID publicId, org.springframework.web.multipart.MultipartFile file, String description);

    void signContract(UUID publicId);
    void signContract(UUID publicId, org.springframework.web.multipart.MultipartFile signatureFile);

    Optional<ContractSearchDTO> searchByPersonDocumentNumber(String personDocumentNumber);

    void cancelContract(UUID publicId);

    void updateExtendedEndDate(UUID contractPublicId, java.time.LocalDate newEndDate);
}
