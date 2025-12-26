package com.agropay.core.shared.exceptions;

public class NoChangesDetectedException extends GenericException {
    public NoChangesDetectedException(String message) {
        super(message);
    }

    public NoChangesDetectedException(String message, Object... args) {
        super(message, args);
    }
}
