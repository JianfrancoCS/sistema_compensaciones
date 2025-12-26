package com.agropay.core.validation.mapper;

import com.agropay.core.validation.domain.ValidationMethodEntity;
import com.agropay.core.validation.model.validationmethod.ValidationMethodSelectOptionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IValidationMethodMapper {
    ValidationMethodSelectOptionDTO toSelectOptionDTO(ValidationMethodEntity entity);
}