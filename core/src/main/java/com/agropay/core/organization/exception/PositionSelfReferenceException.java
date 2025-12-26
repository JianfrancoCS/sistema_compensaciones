package com.agropay.core.organization.exception;

import com.agropay.core.shared.exceptions.GenericException;

public class PositionSelfReferenceException extends GenericException {
    public PositionSelfReferenceException(String message, Object... args) {
        super(message, args);
    }
}
