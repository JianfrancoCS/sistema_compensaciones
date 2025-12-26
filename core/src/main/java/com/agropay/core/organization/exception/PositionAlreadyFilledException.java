package com.agropay.core.organization.exception;

import com.agropay.core.shared.exceptions.GenericException;

public class PositionAlreadyFilledException extends GenericException {
    public PositionAlreadyFilledException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
