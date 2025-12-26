package com.agropay.core.assignment.mapper;

import com.agropay.core.assignment.domain.LaborEntity;
import com.agropay.core.assignment.model.labor.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ILaborMapper {

    LaborEntity toEntity(CreateLaborRequest request);

    void updateEntityFromRequest(UpdateLaborRequest request, @MappingTarget LaborEntity entity);

    @Mapping(target = "laborUnitPublicId", source = "laborUnit.publicId")
    CommandLaborResponse toResponse(LaborEntity entity);

    @Mapping(target = "laborUnitName", source = "laborUnit.name")
    LaborListDTO toListDTO(LaborEntity entity);

    List<LaborListDTO> toListDTOs(List<LaborEntity> entities);

    List<LaborSelectOptionDTO> toSelectOptionDTOs(List<LaborEntity> entities);

    default PagedResult<LaborListDTO> toPagedDTO(Page<LaborEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}