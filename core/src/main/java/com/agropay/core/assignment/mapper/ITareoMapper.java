package com.agropay.core.assignment.mapper;

import com.agropay.core.assignment.domain.TareoEntity;
import com.agropay.core.assignment.model.tareo.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ITareoMapper {

    @Mapping(target = "laborName", source = "labor.name")
    @Mapping(target = "loteName", source = "lote.name")
    @Mapping(target = "loteSubsidiaryName", source = "subsidiary.name")
    @Mapping(target = "employeeCount", ignore = true)
    TareoListDTO toListDTO(TareoEntity entity);

    default PagedResult<TareoListDTO> toPagedDTO(Page<TareoEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}