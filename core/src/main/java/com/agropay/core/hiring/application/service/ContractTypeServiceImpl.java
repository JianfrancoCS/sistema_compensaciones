package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.application.usecase.IContractTypeUseCase;
import com.agropay.core.hiring.domain.ContractTypeEntity;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.hiring.mapper.IContractTypeMapper;
import com.agropay.core.hiring.model.contracttype.ContractTypeSelectOptionDTO;
import com.agropay.core.hiring.persistence.IContractTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractTypeServiceImpl implements IContractTypeUseCase {

    private final IContractTypeRepository contractTypeRepository;
    private final IContractTypeMapper contractTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ContractTypeSelectOptionDTO> getSelectOptions() {
        log.info("Fetching select options for contract types");
        List<ContractTypeEntity> contractTypes = contractTypeRepository.findAll();
        return contractTypeMapper.toSelectOptionDTOs(contractTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractTypeEntity findByPublicId(UUID publicId) {
        return contractTypeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.contract-type.not-found", publicId));
    }
}