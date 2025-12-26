package com.agropay.core.hiring.mapper;

import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.hiring.model.variable.CommandVariableResponse;
import com.agropay.core.hiring.model.variable.VariableListDTO;
import com.agropay.core.hiring.model.variable.VariableSelectOptionDTO;
import com.agropay.core.hiring.model.variable.VariableWithValidationDTO;
import com.agropay.core.hiring.model.variable.AssociatedMethodDTO;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IVariableMapper {
    @Mapping(target = "isRequired", expression = "java(entity.getDefaultValue() == null)")
    VariableSelectOptionDTO toSelectOptionDTO(VariableEntity entity);

    List<VariableSelectOptionDTO> toSelectOptionDTOs(List<VariableEntity> entities);

    @Mapping(target = "validationMethodsCount", ignore = true)
    VariableListDTO toListDTO(VariableEntity entity);

    List<VariableListDTO> toListDTOs(List<VariableEntity> entities);

    default VariableListDTO toListDTOWithCount(VariableEntity entity, Long validationMethodsCount) {
        return new VariableListDTO(
            entity.getPublicId(),
            entity.getCode(),
            entity.getName(),
            entity.getDefaultValue(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            validationMethodsCount != null ? validationMethodsCount : 0L
        );
    }

    CommandVariableResponse toCommandResponse(VariableEntity entity);

    @Mapping(target = "finalRegex", source = "dynamicVariable.finalRegex")
    @Mapping(target = "errorMessage", source = "dynamicVariable.errorMessage")
    @Mapping(target = "methods", ignore = true)
    VariableWithValidationDTO toVariableWithValidationDTO(VariableEntity entity);

    default PagedResult<VariableListDTO> toPagedDTO(Page<VariableEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}
