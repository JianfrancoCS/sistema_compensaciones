package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.domain.AddendumEntity;

import java.util.Map;

public interface IAddendumVariableValueUseCase {
    void create(AddendumEntity addendum, Map<String, String> variables);
    void update(AddendumEntity addendum, Map<String, String> variables);
}