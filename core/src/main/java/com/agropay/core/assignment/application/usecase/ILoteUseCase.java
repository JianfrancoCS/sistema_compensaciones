package com.agropay.core.assignment.application.usecase;

import com.agropay.core.assignment.domain.LoteEntity;
import com.agropay.core.assignment.model.lote.*;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ILoteUseCase {

    CommandLoteResponse create(CreateLoteRequest request);

    CommandLoteResponse update(UUID publicId, UpdateLoteRequest request);

    void delete(UUID publicId);

    PagedResult<LoteListDTO> findAllPaged(String name, UUID subsidiaryPublicId, Pageable pageable);

    CommandLoteResponse findById(UUID publicId);

    LoteEntity findByPublicId(UUID publicId);

    List<LoteSyncResponse> getAllForSync();
}