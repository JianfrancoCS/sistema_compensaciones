package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.application.usecase.IAddendumVariableValueUseCase;
import com.agropay.core.hiring.domain.AddendumEntity;
import com.agropay.core.hiring.domain.AddendumVariableValueEntity;
import com.agropay.core.hiring.domain.AddendumVariableValueId;
import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.hiring.persistence.IAddendumVariableValueRepository;
import com.agropay.core.hiring.persistence.IVariableRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AddendumVariableValueServiceImpl implements IAddendumVariableValueUseCase {

    private final IAddendumVariableValueRepository addendumVariableValueRepository;
    private final IVariableRepository iVariableRepository;
    private final String deletedBy = "anonymous";

    @Override
    public void create(AddendumEntity addendum, Map<String, String> variables) {
        variables.forEach((code, value) -> {
            VariableEntity variable = iVariableRepository.findByCode(code)
                    .orElseThrow(() -> new RuntimeException("Variable not found: " + code));

            // Usar el campo id directamente (Long) en lugar de getId() que devuelve String
            AddendumVariableValueId id = new AddendumVariableValueId(Long.parseLong(addendum.getId()), variable.getId());

            AddendumVariableValueEntity addendumVariableValue = new AddendumVariableValueEntity();
            addendumVariableValue.setId(id);
            addendumVariableValue.setAddendum(addendum);
            addendumVariableValue.setVariable(variable);
            addendumVariableValue.setValue(value);

            addendumVariableValueRepository.save(addendumVariableValue);
        });
    }

    @Override
    public void update(AddendumEntity addendum, Map<String, String> newVariablesMap) {
        Map<String, AddendumVariableValueEntity> existingVariablesMap = addendum.getVariableValues().stream()
                .collect(Collectors.toMap(v -> v.getVariable().getCode(), Function.identity()));

        // Soft delete variables that are no longer in the request
        existingVariablesMap.keySet().stream()
                .filter(code -> !newVariablesMap.containsKey(code))
                .map(existingVariablesMap::get)
                .forEach(entity -> addendumVariableValueRepository.softDelete(entity.getId(), deletedBy));

        // Add new or update existing variables
        newVariablesMap.forEach((code, value) -> {
            AddendumVariableValueEntity existingEntity = existingVariablesMap.get(code);
            if (existingEntity != null) {
                // Update existing value if it has changed
                if (!existingEntity.getValue().equals(value)) {
                    existingEntity.setValue(value);
                    addendumVariableValueRepository.save(existingEntity);
                }
            } else {
                // Add new variable
                VariableEntity variable = iVariableRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Variable not found: " + code));
                // Usar getEntityId() que devuelve Long (getId() devuelve String para IImageable)
                AddendumVariableValueId id = new AddendumVariableValueId(addendum.getEntityId(), variable.getId());
                AddendumVariableValueEntity newEntity = new AddendumVariableValueEntity(id, addendum, variable, value);
                addendumVariableValueRepository.save(newEntity);
            }
        });
    }
}