package com.agropay.core.hiring.mapper;

import com.agropay.core.hiring.domain.ContractTemplateEntity;
import com.agropay.core.hiring.domain.ContractTemplateVariableEntity;
import com.agropay.core.hiring.model.contracttemplate.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {IContractTypeMapper.class} // Assuming you might need to map related entities
)
public interface IContractTemplateMapper {

    @Mapping(target = "code", ignore = true) // Code will be generated in the service
    @Mapping(target = "contractType", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "variables", ignore = true)
    ContractTemplateEntity toEntity(CreateContractTemplateRequest request);

    @Mapping(target = "code", ignore = true) // Code is not updatable
    @Mapping(target = "contractType", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "variables", ignore = true)
    void updateEntityFromRequest(UpdateContractTemplateRequest request, @MappingTarget ContractTemplateEntity entity);

    @Mapping(source = "code", target = "code")
    @Mapping(source = "entity.contractType.publicId", target = "contractTypePublicId")
    @Mapping(source = "entity.state.publicId", target = "statePublicId")
    @Mapping(source = "entity.variables", target = "variables")
    CommandContractTemplateResponse toCommandResponse(ContractTemplateEntity entity);

    @Mapping(source = "code", target = "code")
    @Mapping(source = "entity.contractType.name", target = "contractTypeName")
    @Mapping(source = "entity.state.name", target = "stateName")
    ContractTemplateListDTO toListDTO(ContractTemplateEntity entity);

    List<ContractTemplateListDTO> toListDTOs(List<ContractTemplateEntity> entities);

    ContractTemplateSelectOptionDTO toSelectOptionDTO(ContractTemplateEntity entity);

    List<ContractTemplateSelectOptionDTO> toSelectOptionDTOs(List<ContractTemplateEntity> entities);

    @Mapping(source = "variable.publicId", target = "publicId")
    @Mapping(source = "variable.code", target = "code")
    @Mapping(source = "variable.name", target = "name")
    @Mapping(source = "variable.defaultValue", target = "defaultValue")
    @Mapping(source = "variable.dynamicVariable.finalRegex", target = "validationRegex")
    @Mapping(source = "variable.dynamicVariable.errorMessage", target = "validationErrorMessage")
    @Mapping(target = "hasValidation", expression = "java(entity.getVariable().getDynamicVariable() != null)")
    ContractTemplateVariableDTO toVariableDTO(ContractTemplateVariableEntity entity);

    List<ContractTemplateVariableDTO> toVariableDTOs(Set<ContractTemplateVariableEntity> entities);

    default PagedResult<ContractTemplateListDTO> toPagedDTO(Page<ContractTemplateEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}
