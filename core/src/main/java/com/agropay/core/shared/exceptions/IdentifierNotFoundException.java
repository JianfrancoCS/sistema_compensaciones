package com.agropay.core.shared.exceptions;


public class IdentifierNotFoundException extends GenericException {
    public IdentifierNotFoundException(String message) {
        super(message);
    }
    public IdentifierNotFoundException(String message, Object... args) {
        super(message, args);
    }

}
