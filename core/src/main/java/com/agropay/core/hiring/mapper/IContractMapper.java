package com.agropay.core.hiring.mapper;

import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.hiring.domain.ContractVariableValueEntity;
import com.agropay.core.hiring.model.contract.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IContractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contractType", ignore = true)
    @Mapping(target = "state", ignore = true)
    ContractEntity toEntity(CreateContractRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UpdateContractRequest request, @MappingTarget ContractEntity entity);

    @Mapping(source = "publicId", target = "publicId")
    @Mapping(source = "contractType.publicId", target = "contractTypePublicId")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "subsidiary", target = "subsidiary")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "template", target = "template")
    @Mapping(source = "variableValues", target = "variables")
    CommandContractResponse toCommandResponse(ContractEntity entity);

    @Mapping(source = "publicId", target = "publicId")
    CommandContractResponse.StateInfo toStateInfo(com.agropay.core.states.domain.StateEntity state);

    @Mapping(source = "publicId", target = "publicId")
    CommandContractResponse.SubsidiaryInfo toSubsidiaryInfo(com.agropay.core.organization.domain.SubsidiaryEntity subsidiary);

    @Mapping(source = "publicId", target = "publicId")
    @Mapping(source = "area.publicId", target = "areaPublicId")
    CommandContractResponse.PositionInfo toPositionInfo(com.agropay.core.organization.domain.PositionEntity position);

    @Mapping(source = "publicId", target = "publicId")
    @Mapping(source = "contractType.publicId", target = "contractTypePublicId")
    CommandContractResponse.TemplateInfo toTemplateInfo(com.agropay.core.hiring.domain.ContractTemplateEntity template);

    @Mapping(source = "variable.code", target = "code")
    ContractVariableValuePayload toPayload(ContractVariableValueEntity entity);

    List<ContractVariableValuePayload> toPayloads(List<ContractVariableValueEntity> entities);

    @Mapping(source = "entity.contractType.name", target = "contractTypeName")
    @Mapping(source = "entity.state.name", target = "stateName")
    @Mapping(source = "entity.personDocumentNumber", target = "personDocumentNumber")
    @Mapping(source = "isSigned", target = "isSigned")
    @Mapping(source = "hasImages", target = "hasImages")
    ContractListDTO toListDTO(ContractEntity entity, boolean isSigned, boolean hasImages);

    @Mapping(source = "entity.personDocumentNumber", target = "personDocumentNumber")
    @Mapping(source = "entity.contractType.publicId", target = "contractTypePublicId")
    @Mapping(source = "entity.contractType.name", target = "contractTypeName")
    @Mapping(source = "entity.state.publicId", target = "statePublicId")
    @Mapping(source = "entity.state.name", target = "stateName")
    @Mapping(source = "imageUrls", target = "imageUrls")
    ContractDetailsDTO toDetailsDTO(ContractEntity entity, List<ContractDetailsDTO.ContractImageDTO> imageUrls);

    @Mapping(source = "entity.personDocumentNumber", target = "personDocumentNumber")
    @Mapping(source = "entity.contractType.name", target = "contractTypeName")
    @Mapping(source = "entity.state.name", target = "stateName")
    @Mapping(source = "imageUrls", target = "imageUrls")
    @Mapping(target = "personFullName", ignore = true)
    ContractSearchDTO toSearchDTO(ContractEntity entity, List<ContractSearchDTO.ContractImageDTO> imageUrls);
}
