package com.agropay.core.validation.application.service;

import com.agropay.core.validation.application.usecase.IValidationMethodUseCase;
import com.agropay.core.validation.mapper.IValidationMethodMapper;
import com.agropay.core.validation.model.validationmethod.ValidationMethodSelectOptionDTO;
import com.agropay.core.validation.persistence.IValidationMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ValidationMethodServiceImpl implements IValidationMethodUseCase {

    private final IValidationMethodRepository validationMethodRepository;
    private final IValidationMethodMapper validationMethodMapper;

    @Override
    public List<ValidationMethodSelectOptionDTO> getSelectOptions() {
        log.info("Fetching all validation method select options");
        return validationMethodRepository.findAll().stream()
            .map(validationMethodMapper::toSelectOptionDTO)
            .toList();
    }
}