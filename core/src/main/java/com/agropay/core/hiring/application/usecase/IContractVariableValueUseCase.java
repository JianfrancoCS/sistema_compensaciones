package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.domain.ContractEntity;

import java.util.Map;

public interface IContractVariableValueUseCase {
    void create(ContractEntity contract, Map<String, String> variables);
    void update(ContractEntity contract, Map<String, String> variables);
}
