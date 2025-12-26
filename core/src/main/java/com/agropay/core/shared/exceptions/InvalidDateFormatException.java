package com.agropay.core.shared.exceptions;

public class InvalidDateFormatException extends GenericException {
    public InvalidDateFormatException(String message, Object[] args) {
        super(message, args);
    }
}
