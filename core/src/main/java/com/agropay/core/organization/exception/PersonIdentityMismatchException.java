package com.agropay.core.organization.exception;

import com.agropay.core.shared.exceptions.GenericException;

public class PersonIdentityMismatchException extends GenericException {
    public PersonIdentityMismatchException(String message, Object... args) {
        super(message);
    }
}
