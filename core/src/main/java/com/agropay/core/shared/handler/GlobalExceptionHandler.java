package com.agropay.core.shared.handler;

import com.agropay.core.images.constant.Bucket;
import com.agropay.core.shared.exceptions.*;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.MessageResolver;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageResolver messageSource;

    @ExceptionHandler(IdentifierNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleIdentifierNotFound(IdentifierNotFoundException ex){
        String message = messageSource.getMessage(ex.getMessage(), ex.getParameters(), LocaleContextHolder.getLocale());
        log.warn(message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.failure(message));
    }

    @ExceptionHandler(ProvidersExternalException.class)
    public ResponseEntity<ApiResult<Void>> handleProvidersExternal(ProvidersExternalException ex){
        String message = messageSource.getMessage(ex.getMessage(), ex.getParameters());
        log.error(message, ex);
        return ResponseEntity.status(502).body(ApiResult.failure(message));
    }

    @ExceptionHandler(NoChangesDetectedException.class)
    public ResponseEntity<ApiResult<Void>> handleNoChangesDetected(NoChangesDetectedException ex){
        String message = messageSource.getMessage(ex.getMessage(), ex.getParameters());
        log.warn(message);
        return ResponseEntity.badRequest().body(ApiResult.failure(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        String message = messageSource.getMessage("exception.method-argument-not-valid-exception", null, LocaleContextHolder.getLocale());
        ex.getBindingResult().getAllErrors().forEach((error)->{
            String fieldName = "";
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } else {
                fieldName = error.getObjectName();
            }
            // Intentar resolver el mensaje desde el messageSource si está disponible
            String errorMessage = error.getDefaultMessage();
            try {
                // Si el mensaje tiene formato {key}, intentar resolverlo
                if (errorMessage != null && errorMessage.startsWith("{") && errorMessage.endsWith("}")) {
                    String messageKey = errorMessage.substring(1, errorMessage.length() - 1);
                    errorMessage = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
                }
            } catch (Exception e) {
                // Si no se puede resolver, usar el mensaje por defecto
                log.debug("Could not resolve message key: {}", errorMessage);
            }
            errors.put(fieldName, errorMessage);
        });
        log.warn("{}: {}", message, errors);
        return ResponseEntity.badRequest().body(ApiResult.failure(message,errors));
    };

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().iterator().next().getMessage();
        log.warn("Constraint violation: {}", message);
        return new ResponseEntity<>(ApiResult.failure(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReferentialIntegrityException.class)
    public ResponseEntity<ApiResult<Void>> handleReferentialIntegrity(ReferentialIntegrityException ex){
        String message = messageSource.getMessage(ex.getMessage(), ex.getParameters());
        log.warn(message);
        return ResponseEntity.badRequest().body(ApiResult.failure(message));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex){
        String message = messageSource.getMessage(ex.getMessage(),ex.getParameters());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
                .body(ApiResult.failure(message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message;
        if (ex.getRequiredType() == Bucket.class) {
            String bucketsName = Arrays.stream(Bucket.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.joining(", "));

            message = messageSource.getMessage(
                    "exception.images.method-argument-type-mismatch",
                    bucketsName
            );
        } else {
            message = messageSource.getMessage("exception.method-argument-type-mismatch");
        }
        return ResponseEntity.badRequest().body(ApiResult.failure(message));
    }

    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidDateFormat(InvalidDateFormatException ex) {
        String message = messageSource.getMessage(ex.getMessage(), ex.getParameters());
        log.warn("Invalid date format exception: {}", message);
        return ResponseEntity.badRequest().body(ApiResult.failure(message));
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidSortField(InvalidSortFieldException ex) {
        String message = messageSource.getMessage(ex.getMessage(), ex.getParameters());
        log.warn("Invalid sort field exception: {}", message);
        return ResponseEntity.badRequest().body(ApiResult.failure(message));
    }

    @ExceptionHandler(UniqueValidationException.class)
    public ResponseEntity<ApiResult<Void>> handleUniqueValidation(UniqueValidationException ex) {
        // Desempaquetar el array de parámetros correctamente
        Object[] params = ex.getParameters();
        String message = messageSource.getMessage(ex.getMessage(), params != null ? params : new Object[0]);
        log.warn("Unique validation exception: {}", message);
        return ResponseEntity.badRequest().body(ApiResult.failure(message));
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiResult<Void>> handleBusinessValidation(BusinessValidationException ex) {
        // Desempaquetar el array de parámetros correctamente
        Object[] params = ex.getParameters();
        String message = messageSource.getMessage(ex.getMessage(), params != null ? params : new Object[0]);
        log.warn("Business validation exception: {}", message);
        return ResponseEntity.badRequest().body(ApiResult.failure(message));
    }
}
