package com.agropay.core.organization.exception;

import com.agropay.core.shared.exceptions.GenericException;
import org.springframework.http.HttpStatus;

public class InvalidEmployeeStateException extends GenericException {
    public InvalidEmployeeStateException(String messageKey, String currentState, String requiredState) {
        super(messageKey, HttpStatus.BAD_REQUEST, currentState, requiredState);
    }
}