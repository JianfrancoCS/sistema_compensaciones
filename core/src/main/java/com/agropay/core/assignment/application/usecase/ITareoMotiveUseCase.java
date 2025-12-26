package com.agropay.core.assignment.application.usecase;

import com.agropay.core.assignment.domain.TareoMotiveEntity;
import com.agropay.core.assignment.model.tareomotive.*;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ITareoMotiveUseCase {

    CommandTareoMotiveResponse create(CreateTareoMotiveRequest request);

    CommandTareoMotiveResponse update(UUID publicId, UpdateTareoMotiveRequest request);

    void delete(UUID publicId);

    List<TareoMotiveSelectOptionDTO> getSelectOptions();

    PagedResult<TareoMotiveListDTO> findAllPaged(String name, Boolean isPaid, Pageable pageable);

    TareoMotiveEntity findByPublicId(UUID publicId);

    List<TareoMotiveSyncReponse> findAllForSync();
}