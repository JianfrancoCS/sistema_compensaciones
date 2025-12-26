package com.agropay.core.assignment.model.tareo;

import com.agropay.core.shared.batch.BatchSummary;

import java.util.UUID;

/**
 * Datos del resultado de procesar un tareo en batch.
 * Se usa como tipo genérico T en BatchItemResult<T>
 */
public record BatchTareoResultData(
        UUID tareoPublicId,
        BatchSummary employeeSummary
) {
    public static BatchTareoResultData of(UUID tareoPublicId, BatchSummary employeeSummary) {
        return new BatchTareoResultData(tareoPublicId, employeeSummary);
    }
}