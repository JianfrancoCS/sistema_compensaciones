package com.agropay.core.shared.utils;

import java.time.Instant;
import java.util.Map;

public record ApiResult<T>(
        boolean success,
        String message,
        T data,
        Map<String, String> errors,
        Instant timeStamp
) {
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, "Operación completada satisfactoriamente", data, null, Instant.now());
    }

    public static <T> ApiResult<T> success(T data, String message) {
        return new ApiResult<>(true, message, data, null, Instant.now());
    }

    public static <T> ApiResult<T> failure(String message, Map<String, String> errors) {
        return new ApiResult<>(false, message, null, errors, Instant.now());
    }

    public static <T> ApiResult<T> failure(String message) {
        return failure(message, null);
    }
}