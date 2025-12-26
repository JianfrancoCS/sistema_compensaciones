package com.agropay.core.assignment.mapper;

import com.agropay.core.assignment.domain.LaborUnitEntity;
import com.agropay.core.assignment.model.laborunit.*;
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
public interface ILaborUnitMapper {

    LaborUnitEntity toEntity(CreateLaborUnitRequest request);

    void updateEntityFromRequest(UpdateLaborUnitRequest request, @MappingTarget LaborUnitEntity entity);

    CommandLaborUnitResponse toResponse(LaborUnitEntity entity);

    LaborUnitListDTO toListDTO(LaborUnitEntity entity);

    List<LaborUnitListDTO> toListDTOs(List<LaborUnitEntity> entities);

    List<LaborUnitSelectOptionDTO> toSelectOptionDTOs(List<LaborUnitEntity> entities);

    default PagedResult<LaborUnitListDTO> toPagedDTO(Page<LaborUnitEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}