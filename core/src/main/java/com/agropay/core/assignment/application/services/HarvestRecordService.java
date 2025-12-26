package com.agropay.core.assignment.application.services;

import com.agropay.core.assignment.application.usecase.IHarvestRecordUseCase;
import com.agropay.core.assignment.domain.*;
import com.agropay.core.assignment.model.harvest.*;
import com.agropay.core.assignment.persistence.*;
import com.agropay.core.shared.batch.BatchItemResult;
import com.agropay.core.shared.batch.BatchResponse;
import com.agropay.core.shared.batch.ErrorDetail;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HarvestRecordService implements IHarvestRecordUseCase {

    private final IHarvestRecordRepository harvestRecordRepository;
    private final IQrCodeRepository qrCodeRepository;

    @Override
    @Transactional
    public BatchResponse<BatchHarvestResultData> batchSync(BatchHarvestSyncRequest request) {
        log.info("Attempting batch sync with {} harvest records", request.records().size());

        List<BatchItemResult<BatchHarvestResultData>> results = new ArrayList<>();

        for (BatchHarvestRecordData recordData : request.records()) {
            try {
                BatchItemResult<BatchHarvestResultData> result = processSingleRecord(recordData);
                results.add(result);
            } catch (Exception e) {
                log.error("Unexpected error processing harvest record with temporalId: {}", recordData.temporalId(), e);
                results.add(BatchItemResult.error(
                        recordData.temporalId(),
                        "UNEXPECTED_ERROR",
                        "Unexpected error: " + e.getMessage()
                ));
            }
        }

        log.info("Batch sync completed. Total: {}, Successful: {}, Failed: {}",
                results.size(),
                results.stream().filter(BatchItemResult::isSuccess).count(),
                results.stream().filter(BatchItemResult::isError).count());

        return BatchResponse.of(results);
    }

    private BatchItemResult<BatchHarvestResultData> processSingleRecord(BatchHarvestRecordData recordData) {
        log.info("Processing harvest record with temporalId: {}", recordData.temporalId());

        // UPSERT: Buscar por temporalId
        HarvestRecordEntity existingRecord = harvestRecordRepository
                .findByTemporalIdAndDeletedAtIsNull(recordData.temporalId())
                .orElse(null);

        if (existingRecord != null) {
            // Ya existe, idempotencia
            log.info("Harvest record with temporalId {} already exists, skipping", recordData.temporalId());
            return BatchItemResult.success(
                    recordData.temporalId(),
                    BatchHarvestResultData.of(existingRecord.getPublicId())
            );
        }

        // Validar que el código QR exista
        QrCodeEntity qrCode = qrCodeRepository.findByPublicId(recordData.qrCodePublicId())
                .orElse(null);

        if (qrCode == null) {
            return BatchItemResult.error(
                    recordData.temporalId(),
                    "QR_CODE_NOT_FOUND",
                    "QR code with public ID " + recordData.qrCodePublicId() + " does not exist"
            );
        }

        // Validar que el QR no haya sido usado
        if (qrCode.getIsUsed()) {
            return BatchItemResult.error(
                    recordData.temporalId(),
                    "QR_CODE_ALREADY_USED",
                    "QR code " + recordData.qrCodePublicId() + " has already been used"
            );
        }

        // Crear el registro de cosecha
        HarvestRecordEntity harvestRecord = new HarvestRecordEntity();
        harvestRecord.setTemporalId(recordData.temporalId());
        harvestRecord.setQrCode(qrCode);
        harvestRecord.setScannedAt(recordData.scannedAt());

        HarvestRecordEntity savedRecord = harvestRecordRepository.save(harvestRecord);

        // Marcar el QR code como usado
        qrCode.setIsUsed(true);
        qrCodeRepository.save(qrCode);

        log.info("Successfully created harvest record with publicId: {}, temporalId: {} and marked QR code as used",
                savedRecord.getPublicId(), savedRecord.getTemporalId());

        return BatchItemResult.success(
                recordData.temporalId(),
                BatchHarvestResultData.of(savedRecord.getPublicId())
        );
    }
}
