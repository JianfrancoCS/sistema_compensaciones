package com.agropay.core.hiring.mapper;

import com.agropay.core.hiring.domain.AddendumTypeEntity;
import com.agropay.core.hiring.model.addendumtype.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IAddendumTypeMapper {

    @Mapping(target = "code", ignore = true) // Code will be generated in the service
    AddendumTypeEntity toEntity(CreateAddendumTypeRequest request);

    @Mapping(target = "code", ignore = true) // Code is not updatable
    void updateEntityFromRequest(UpdateAddendumTypeRequest request, @MappingTarget AddendumTypeEntity entity);

    CommandAddendumTypeResponse toCommandResponse(AddendumTypeEntity entity);

    AddendumTypeListDTO toListDTO(AddendumTypeEntity entity);

    List<AddendumTypeListDTO> toListDTOs(List<AddendumTypeEntity> entities);

    AddendumTypeDetailsDTO toDetailsDTO(AddendumTypeEntity entity);

    AddendumTypeSelectOptionDTO toSelectOptionDTO(AddendumTypeEntity entity);

    List<AddendumTypeSelectOptionDTO> toSelectOptionDTOs(List<AddendumTypeEntity> entities);

    default PagedResult<AddendumTypeListDTO> toPagedDTO(Page<AddendumTypeEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}