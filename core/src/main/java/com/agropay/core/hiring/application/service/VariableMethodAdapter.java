package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.model.variable.VariableMethodRequest;
import com.agropay.core.validation.application.service.VariableValidationService.MethodValidationRequest;

import java.util.UUID;

public class VariableMethodAdapter implements MethodValidationRequest {
    private final VariableMethodRequest delegate;

    public VariableMethodAdapter(VariableMethodRequest delegate) {
        this.delegate = delegate;
    }

    @Override
    public UUID getMethodPublicId() {
        return delegate.methodPublicId();
    }

    @Override
    public String getValue() {
        return delegate.value();
    }

    @Override
    public Integer getExecutionOrder() {
        return delegate.executionOrder();
    }
}