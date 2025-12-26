package com.agropay.core.organization.exception;

import com.agropay.core.shared.exceptions.GenericException;

public class PersonAlreadyExistsException extends GenericException {
    public PersonAlreadyExistsException(String key, Object... args) {
        super(key, args);
    }
}
