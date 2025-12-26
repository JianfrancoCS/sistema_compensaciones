package com.agropay.core.organization.mapper;

import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.model.position.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IPositionMapper {

    @Mapping(target = "area", ignore = true)
    @Mapping(target = "requiresManager", source = "requiresManager")
    @Mapping(target = "unique", source = "unique")
    @Mapping(target = "requiredManagerPosition", ignore = true)
    PositionEntity toEntity(CreatePositionRequest request);

    @Mapping(target = "area", ignore = true)
    @Mapping(target = "requiresManager", source = "requiresManager")
    @Mapping(target = "unique", source = "unique")
    @Mapping(target = "requiredManagerPosition", ignore = true)
    void updateEntityFromRequest(UpdatePositionRequest request, @MappingTarget PositionEntity entity);

    @Mapping(source = "area.publicId", target = "areaPublicId")
    @Mapping(target = "requiresManager", source = "requiresManager")
    @Mapping(target = "unique", source = "unique")
    @Mapping(source = "requiredManagerPosition.publicId", target = "requiredManagerPositionPublicId")
    CommandPositionResponse toResponse(PositionEntity entity);

    @Mapping(target = "requiresManager", source = "requiresManager")
    @Mapping(target = "unique", source = "unique")
    @Mapping(target = "requiredManagerPosition.publicId", source = "requiredManagerPosition.publicId")
    @Mapping(target = "requiredManagerPosition.name", source = "requiredManagerPosition.name")
    @Mapping(target = "area.name", source = "area.name")
    PositionListDTO toListDTO(PositionEntity entity);

    @Mapping(target = "area", source = "entity.area")
    @Mapping(target = "employeeCount", expression = "java(employeeCount)")
    PositionDetailsDTO toDetailsDTO(PositionEntity entity, long employeeCount);

    List<PositionListDTO> toListDTOs(List<PositionEntity> entities);

    List<PositionSelectOptionDTO> toSelectOptionDTOs(List<PositionEntity> entities);

    default PagedResult<PositionListDTO> toPagedDTO(Page<PositionEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}
