package com.agropay.core.shared.exceptions;

/**
 * Exception thrown when an invalid field name is used for sorting operations
 */
public class InvalidSortFieldException extends GenericException {
    
    public InvalidSortFieldException(String message) {
        super(message);
    }

    public InvalidSortFieldException(String message, Object... parameters) {
        super(message, parameters);
    }

    public InvalidSortFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSortFieldException(String message, Throwable cause, Object... parameters) {
        super(message, cause, parameters);
    }
}