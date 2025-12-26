package com.agropay.core.organization.mapper;

import com.agropay.core.organization.domain.AreaEntity;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.model.area.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IAreaMapper {

    AreaEntity toEntity(CreateAreaRequest request);

    void updateEntityFromRequest(UpdateAreaRequest request, @MappingTarget AreaEntity entity);

    CommandAreaResponse toResponse(AreaEntity entity);

    AreaDetailsDTO toDetailsDTO(AreaEntity entity);

    AreaListDTO toListDTO(AreaEntity entity);

    List<AreaListDTO> toListDTOs(List<AreaEntity> entities);

    List<AreaSelectOptionDTO> toSelectOptionDTOs(List<AreaEntity> entities);

    AreaDetailsDTO.PositionInfo toPositionInfo(PositionEntity position);

    default PagedResult<AreaListDTO> toPagedDTO(Page<AreaEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}
