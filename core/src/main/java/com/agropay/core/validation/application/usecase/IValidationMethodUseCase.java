package com.agropay.core.validation.application.usecase;

import com.agropay.core.validation.model.validationmethod.ValidationMethodSelectOptionDTO;

import java.util.List;

public interface IValidationMethodUseCase {
    List<ValidationMethodSelectOptionDTO> getSelectOptions();
}