package com.agropay.core.shared.batch;

public record ErrorDetail(
        String identifier,
        String errorCode,
        String message
) {
    public static ErrorDetail of(String identifier, String errorCode, String message) {
        return new ErrorDetail(identifier, errorCode, message);
    }
}