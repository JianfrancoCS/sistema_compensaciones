package com.agropay.core.auth.mapper;

import com.agropay.core.auth.domain.ContainerEntity;
import com.agropay.core.auth.model.container.*;
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
public interface IContainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    ContainerEntity toEntity(CreateContainerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(UpdateContainerRequest request, @MappingTarget ContainerEntity entity);

    CommandContainerResponse toResponse(ContainerEntity entity);

    ContainerDetailsDTO toDetailsDTO(ContainerEntity entity);

    ContainerListDTO toListDTO(ContainerEntity entity);

    List<ContainerListDTO> toListDTOs(List<ContainerEntity> entities);

    default PagedResult<ContainerListDTO> toPagedDTO(Page<ContainerEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}

