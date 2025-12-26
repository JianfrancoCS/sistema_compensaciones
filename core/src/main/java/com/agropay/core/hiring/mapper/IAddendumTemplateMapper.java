package com.agropay.core.hiring.mapper;

import com.agropay.core.hiring.domain.AddendumTemplateEntity;
import com.agropay.core.hiring.domain.AddendumTemplateVariableEntity;
import com.agropay.core.hiring.model.addendumtemplate.*;
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
    uses = {IAddendumTypeMapper.class} // Using the AddendumTypeMapper for related entities
)
public interface IAddendumTemplateMapper {

    @Mapping(target = "code", ignore = true)
    @Mapping(target = "addendumType", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "variables", ignore = true)
    AddendumTemplateEntity toEntity(CreateAddendumTemplateRequest request);

    @Mapping(target = "code", ignore = true)
    @Mapping(target = "addendumType", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "variables", ignore = true)
    void updateEntityFromRequest(UpdateAddendumTemplateRequest request, @MappingTarget AddendumTemplateEntity entity);

    @Mapping(source = "publicId", target = "id")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "entity.addendumType.publicId", target = "addendumTypePublicId")
    @Mapping(source = "entity.state.publicId", target = "statePublicId")
    @Mapping(source = "entity.variables", target = "variables")
    @Mapping(source = "templateContent", target = "content")
    CommandAddendumTemplateResponse toCommandResponse(AddendumTemplateEntity entity);

    @Mapping(source = "code", target = "code")
    @Mapping(source = "entity.addendumType.name", target = "addendumTypeName")
    @Mapping(source = "entity.state.name", target = "stateName")
    AddendumTemplateListDTO toListDTO(AddendumTemplateEntity entity);

    List<AddendumTemplateListDTO> toListDTOs(List<AddendumTemplateEntity> entities);

    @Mapping(source = "code", target = "code")
    @Mapping(source = "entity.addendumType.name", target = "addendumTypeName")
    @Mapping(source = "entity.addendumType.publicId", target = "addendumTypePublicId")
    @Mapping(source = "entity.state.name", target = "name")
    @Mapping(source = "entity.state.publicId", target = "statePublicId")
    @Mapping(source = "entity.variables", target = "variables")
    AddendumTemplateDetailsDTO toDetailsDTO(AddendumTemplateEntity entity);

    @Mapping(source = "code", target = "code")
    AddendumTemplateSelectOptionDTO toSelectOptionDTO(AddendumTemplateEntity entity);

    List<AddendumTemplateSelectOptionDTO> toSelectOptionDTOs(List<AddendumTemplateEntity> entities);

    @Mapping(source = "variable.publicId", target = "publicId")
    @Mapping(source = "variable.code", target = "code")
    @Mapping(source = "variable.name", target = "name")
    AddendumTemplateVariableDTO toVariableDTO(AddendumTemplateVariableEntity entity);

    List<AddendumTemplateVariableDTO> toVariableDTOs(Set<AddendumTemplateVariableEntity> entities);

    default PagedResult<AddendumTemplateListDTO> toPagedDTO(Page<AddendumTemplateEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}