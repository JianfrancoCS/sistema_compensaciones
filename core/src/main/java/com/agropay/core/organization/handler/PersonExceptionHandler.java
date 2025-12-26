package com.agropay.core.organization.handler;

import com.agropay.core.organization.exception.PersonAlreadyExistsException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.utils.MessageResolver;
import com.agropay.core.shared.utils.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class PersonExceptionHandler {

    private final MessageResolver messageResolver;

    @ExceptionHandler(IdentifierNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handlePersonNotFoundInExternalServiceException(IdentifierNotFoundException ex) {
        String message = messageResolver.getMessage(ex.getMessage(), ex.getParameters());
        return new ResponseEntity<>(ApiResult.failure(message), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PersonAlreadyExistsException.class)
    public ResponseEntity<ApiResult<String>> handlePersonAlreadyExistsException(PersonAlreadyExistsException ex) {
        String message = messageResolver.getMessage(ex.getMessage(), ex.getParameters());
        return new ResponseEntity<>(ApiResult.failure(message), HttpStatus.CONFLICT);
    }
}
