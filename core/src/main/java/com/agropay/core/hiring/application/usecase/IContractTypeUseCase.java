package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.domain.ContractTypeEntity;
import com.agropay.core.hiring.model.contracttype.ContractTypeSelectOptionDTO;

import java.util.List;
import java.util.UUID;

public interface IContractTypeUseCase {
    List<ContractTypeSelectOptionDTO> getSelectOptions();

    ContractTypeEntity findByPublicId(UUID publicId);
}