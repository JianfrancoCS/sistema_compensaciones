package com.agropay.core.organization.exception;

import com.agropay.core.shared.exceptions.GenericException;

public class ExternalServiceException extends GenericException {
    public ExternalServiceException(String message) {
        super(message);
    }
}
