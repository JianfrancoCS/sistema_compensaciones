package com.agropay.core.shared.batch;

import com.fasterxml.jackson.annotation.JsonGetter;
import java.util.List;

/**
 * Resultado genérico del procesamiento de un item individual dentro de un batch.
 *
 * @param <T> Tipo del dato del item procesado (puede ser el item completo o solo datos relevantes)
 */
public record BatchItemResult<T>(
        String identifier,
        ResultStatus status,
        T data,
        List<ErrorDetail> errors
) {
    /**
     * Campo calculado para compatibilidad con el móvil.
     * Un item es exitoso si el status es SUCCESS o PARTIAL_SUCCESS.
     */
    @JsonGetter("success")
    public boolean success() {
        return status == ResultStatus.SUCCESS || status == ResultStatus.PARTIAL_SUCCESS;
    }
    public static <T> BatchItemResult<T> success(String identifier, T data) {
        return new BatchItemResult<>(identifier, ResultStatus.SUCCESS, data, List.of());
    }

    public static <T> BatchItemResult<T> error(String identifier, String errorCode, String message) {
        return new BatchItemResult<>(
                identifier,
                ResultStatus.ERROR,
                null,
                List.of(ErrorDetail.of(identifier, errorCode, message))
        );
    }

    public static <T> BatchItemResult<T> error(String identifier, ErrorDetail error) {
        return new BatchItemResult<>(identifier, ResultStatus.ERROR, null, List.of(error));
    }

    public boolean isSuccess() {
        return status == ResultStatus.SUCCESS;
    }

    public boolean isError() {
        return status == ResultStatus.ERROR;
    }
}