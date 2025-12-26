package com.agropay.core.shared.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Respuesta genérica para operaciones batch.
 *
 * @param <T> Tipo de dato del resultado de cada item
 */
public record BatchResponse<T>(
        ResultStatus status,
        BatchSummary summary,
        @JsonProperty("items") List<BatchItemResult<T>> results
) {
    public static <T> BatchResponse<T> of(List<BatchItemResult<T>> results) {
        int total = results.size();
        int successful = (int) results.stream().filter(BatchItemResult::isSuccess).count();
        int failed = (int) results.stream().filter(BatchItemResult::isError).count();
        int partialSuccess = (int) results.stream()
                .filter(r -> r.status() == ResultStatus.PARTIAL_SUCCESS)
                .count();

        BatchSummary summary = BatchSummary.of(total, successful, failed, partialSuccess);

        ResultStatus status;
        if (failed == 0) {
            status = ResultStatus.SUCCESS;
        } else if (successful == 0) {
            status = ResultStatus.ERROR;
        } else {
            status = ResultStatus.PARTIAL_SUCCESS;
        }

        return new BatchResponse<>(status, summary, results);
    }
}