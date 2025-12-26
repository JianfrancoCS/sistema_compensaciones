package com.agropay.core.assignment.application.usecase;

import com.agropay.core.assignment.domain.TareoEntity;
import com.agropay.core.assignment.model.tareo.*;
import com.agropay.core.shared.batch.BatchResponse;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ITareoUseCase {

    void delete(UUID publicId);

    PagedResult<TareoListDTO> findAllPaged(UUID laborPublicId, UUID subsidiaryPublicId, String createdBy, 
                                            java.time.LocalDate dateFrom, java.time.LocalDate dateTo, 
                                            Boolean isProcessed, Pageable pageable);

    BatchResponse<BatchTareoResultData> batchSync(BatchTareoSyncRequest request);

    TareoEntity findByPublicId(UUID publicId);

    List<EmployeeSyncResponse> getEmployeesForSync(UUID tareoPublicId);

    PagedResult<TareoDailyDTO> findAllDailyPaged(UUID laborPublicId, UUID subsidiaryPublicId, 
                                                 java.time.LocalDate dateFrom, java.time.LocalDate dateTo,
                                                 Boolean isCalculated, Pageable pageable);
}