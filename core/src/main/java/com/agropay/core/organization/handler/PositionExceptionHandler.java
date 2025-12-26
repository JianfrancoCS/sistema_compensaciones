package com.agropay.core.organization.handler;

import com.agropay.core.organization.exception.PositionAlreadyFilledException;
import com.agropay.core.organization.exception.PositionManagerAreaMismatchException;
import com.agropay.core.organization.exception.PositionSelfReferenceException;
import com.agropay.core.shared.utils.MessageResolver;
import com.agropay.core.shared.utils.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class PositionExceptionHandler {

    private final MessageResolver messageResolver;

    @ExceptionHandler(PositionAlreadyFilledException.class)
    public ResponseEntity<ApiResult<String>> handlePositionAlreadyFilledException(PositionAlreadyFilledException ex) {
        String message = messageResolver.getMessage(ex.getMessage(), ex.getParameters());
        return new ResponseEntity<>(ApiResult.failure(message), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PositionManagerAreaMismatchException.class)
    public ResponseEntity<ApiResult<String>> handlePositionManagerAreaMismatchException(PositionManagerAreaMismatchException ex) {
        String message = messageResolver.getMessage(ex.getMessage(), ex.getParameters());
        return new ResponseEntity<>(ApiResult.failure(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PositionSelfReferenceException.class)
    public ResponseEntity<ApiResult<String>> handlePositionSelfReferenceException(PositionSelfReferenceException ex) {
        String message = messageResolver.getMessage(ex.getMessage(), ex.getParameters());
        return new ResponseEntity<>(ApiResult.failure(message), HttpStatus.BAD_REQUEST);
    }
}
