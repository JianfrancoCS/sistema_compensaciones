package com.agropay.core.shared.batch;

/**
 * Resumen del procesamiento de un batch.
 */
public record BatchSummary(
        int total,
        int successful,
        int failed,
        int partialSuccess
) {
    public static BatchSummary of(int total, int successful, int failed) {
        return new BatchSummary(total, successful, failed, 0);
    }
    
    public static BatchSummary of(int total, int successful, int failed, int partialSuccess) {
        return new BatchSummary(total, successful, failed, partialSuccess);
    }

    public boolean hasFailures() {
        return failed > 0;
    }

    public boolean isFullSuccess() {
        return failed == 0 && total > 0;
    }
}