package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.application.usecase.IContractVariableValueUseCase;
import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.hiring.domain.ContractVariableValueEntity;
import com.agropay.core.hiring.domain.ContractVariableValueId;
import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.hiring.persistence.IContractVariableValueRepository;
import com.agropay.core.hiring.persistence.IVariableRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ContractVariableValueServiceImpl implements IContractVariableValueUseCase {

    private final IContractVariableValueRepository contractVariableValueRepository;
    private final IVariableRepository iVariableRepository;
    private final String deletedBy = "anonymous";

    @Override
    public void create(ContractEntity contract, Map<String, String> variables) {
        variables.forEach((code, value) -> {
            VariableEntity variable = iVariableRepository.findByCode(code)
                    .orElseThrow(() -> new RuntimeException("Variable not found: " + code));

            // Usar getEntityId() que devuelve Long (getId() devuelve String para IImageable/IFileable)
            ContractVariableValueId id = new ContractVariableValueId(contract.getEntityId(), variable.getId());

            ContractVariableValueEntity contractVariableValue = new ContractVariableValueEntity();
            contractVariableValue.setId(id);
            contractVariableValue.setContract(contract);
            contractVariableValue.setVariable(variable);
            contractVariableValue.setValue(value);

            contractVariableValueRepository.save(contractVariableValue);
        });
    }

    @Override
    public void update(ContractEntity contract, Map<String, String> newVariablesMap) {
        Map<String, ContractVariableValueEntity> existingVariablesMap = contract.getVariableValues().stream()
                .collect(Collectors.toMap(v -> v.getVariable().getCode(), Function.identity()));

        existingVariablesMap.keySet().stream()
                .filter(code -> !newVariablesMap.containsKey(code))
                .map(existingVariablesMap::get)
                .forEach(entity -> contractVariableValueRepository.softDelete(entity.getId(), deletedBy));

        newVariablesMap.forEach((code, value) -> {
            ContractVariableValueEntity existingEntity = existingVariablesMap.get(code);
            if (existingEntity != null) {
                if (!existingEntity.getValue().equals(value)) {
                    existingEntity.setValue(value);
                    contractVariableValueRepository.save(existingEntity);
                }
            } else {
                VariableEntity variable = iVariableRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Variable not found: " + code));
                // Usar getEntityId() que devuelve Long (getId() devuelve String para IImageable/IFileable)
                ContractVariableValueId id = new ContractVariableValueId(contract.getEntityId(), variable.getId());
                ContractVariableValueEntity newEntity = new ContractVariableValueEntity(id, contract, variable, value);
                contractVariableValueRepository.save(newEntity);
            }
        });
    }
}
