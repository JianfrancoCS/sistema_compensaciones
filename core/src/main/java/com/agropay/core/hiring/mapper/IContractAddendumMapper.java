package com.agropay.core.hiring.mapper;

import com.agropay.core.hiring.domain.AddendumEntity;
import com.agropay.core.hiring.domain.AddendumVariableValueEntity;
import com.agropay.core.hiring.model.addendum.*;
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
    uses = {IAddendumTypeMapper.class, IAddendumTemplateMapper.class}
)
public interface IContractAddendumMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addendumNumber", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "addendumType", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "parentAddendum", ignore = true)
    @Mapping(target = "childAddendums", ignore = true)
    @Mapping(target = "variableValues", ignore = true)
    @Mapping(source = "startDate", target = "effectiveDate")
    AddendumEntity toEntity(CreateAddendumRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addendumNumber", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "addendumType", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "parentAddendum", ignore = true)
    @Mapping(target = "childAddendums", ignore = true)
    @Mapping(target = "variableValues", ignore = true)
    void updateEntityFromRequest(UpdateAddendumRequest request, @MappingTarget AddendumEntity entity);

    @Mapping(source = "publicId", target = "id")
    @Mapping(source = "contract.publicId", target = "contractPublicId")
    @Mapping(source = "addendumType.publicId", target = "addendumTypePublicId")
    @Mapping(source = "state.publicId", target = "statePublicId")
    @Mapping(source = "template.publicId", target = "templatePublicId")
    @Mapping(source = "variableValues", target = "variables")
    CommandAddendumResponse toCommandResponse(AddendumEntity entity);

    @Mapping(source = "variable.code", target = "code")
    @Mapping(source = "value", target = "value")
    AddendumVariableValuePayload toPayload(AddendumVariableValueEntity entity);

    List<AddendumVariableValuePayload> toPayloads(Set<AddendumVariableValueEntity> entities);

    @Mapping(source = "publicId", target = "id")
    @Mapping(source = "contract.publicId", target = "contractPublicId")
    @Mapping(source = "addendumType.publicId", target = "addendumTypePublicId")
    @Mapping(source = "state.publicId", target = "statePublicId")
    @Mapping(source = "template.publicId", target = "templatePublicId")
    @Mapping(source = "variableValues", target = "variables")
    CommandAddendumResponse toCommandAddendumResponse(AddendumEntity entity);

    @Mapping(source = "entity.effectiveDate", target = "startDate")
    @Mapping(source = "entity.contract.contractNumber", target = "contractNumber")
    @Mapping(source = "entity.contract.publicId", target = "contractPublicId")
    @Mapping(source = "entity.addendumType.publicId", target = "addendumTypePublicId")
    @Mapping(source = "entity.addendumType.name", target = "addendumTypeName")
    @Mapping(source = "entity.state.publicId", target = "statePublicId")
    @Mapping(source = "entity.state.name", target = "stateName")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(expression = "java(mapVariablesToString(entity.getVariableValues()))", target = "variables")
    AddendumDetailsDTO toDetailsDTO(AddendumEntity entity, String imageUrl);

    @Mapping(source = "effectiveDate", target = "startDate")
    @Mapping(source = "contract.contractNumber", target = "contractNumber")
    @Mapping(source = "addendumType.name", target = "addendumTypeName")
    @Mapping(source = "state.name", target = "stateName")
    AddendumListDTO toListDTO(AddendumEntity entity);

    default String mapVariablesToString(Set<AddendumVariableValueEntity> variableValues) {
        if (variableValues == null || variableValues.isEmpty()) {
            return "";
        }
        return variableValues.stream()
                .map(v -> v.getVariable().getCode() + ": " + v.getValue())
                .collect(java.util.stream.Collectors.joining(", "));
    }
}