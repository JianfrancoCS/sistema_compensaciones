package com.agropay.core.assignment.application.usecase;

import com.agropay.core.assignment.domain.QrRollEntity;
import com.agropay.core.assignment.model.qrroll.*;
import com.agropay.core.shared.batch.BatchResponse;
import com.agropay.core.shared.utils.PagedResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IQrRollUseCase {

    CommandQrRollResponse create(CreateQrRollRequest request);

    CommandQrRollResponse update(UUID publicId, UpdateQrRollRequest request);

    void delete(UUID publicId);

    void generateQrCodes(UUID rollPublicId, int quantity);

    void printQrCodes(UUID rollPublicId);

    void assignToEmployee(UUID rollPublicId, String employeeDocumentNumber);

    BatchResponse<Void> batchAssign(BatchAssignQrRollsRequest request);

    QrRollEntity findByPublicId(UUID publicId);

    PagedResult<QrRollListDTO> findAllPaged(QrRollPageableRequest request);

    PagedResult<PrintStatsDTO> findPrintStats(PrintStatsPageableRequest request);

    AvailableQrRollsStatsResponse findAvailableRollsStats(LocalDate date);

    List<QrCodeDTO> findQrCodesByRoll(UUID rollPublicId, Boolean isUsed, Boolean isPrinted, LocalDate createdDate);

    void batchGenerateQrCodes(Integer rollsNeeded, Integer codesPerRoll);
}