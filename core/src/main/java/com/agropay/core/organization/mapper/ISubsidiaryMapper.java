package com.agropay.core.organization.mapper;

import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.organization.model.subsidiary.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
public interface ISubsidiaryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "district", ignore = true)
    SubsidiaryEntity toEntity(CreateSubsidiaryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "district", ignore = true)
    void updateEntityFromRequest(UpdateSubsidiaryRequest request, @MappingTarget SubsidiaryEntity entity);

    @Mapping(source = "district.publicId", target = "districtPublicId")
    CommandSubsidiaryResponse toResponse(SubsidiaryEntity entity);

    @Mapping(source = "district.publicId", target = "districtId")
    SubsidiaryDetailsDTO toDetailsDTO(SubsidiaryEntity entity);

    @Mapping(source = "district.name", target = "districtName")
    SubsidiaryListDTO toListDTO(SubsidiaryEntity entity);

    List<SubsidiaryListDTO> toListDTOs(List<SubsidiaryEntity> entities);

    List<SubsidiarySelectOptionDTO> toSelectOptionDTOs(List<SubsidiaryEntity> entities);

    default PagedResult<SubsidiaryListDTO> toPagedDTO(Page<SubsidiaryEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}
