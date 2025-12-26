package com.agropay.core.organization.exception;

import com.agropay.core.shared.exceptions.GenericException;

public class PositionManagerAreaMismatchException extends GenericException {
    public PositionManagerAreaMismatchException(String message, Object... args) {
        super(message, args);
    }
}
