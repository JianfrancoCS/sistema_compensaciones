package com.agropay.core.auth.mapper;

import com.agropay.core.auth.domain.ElementEntity;
import com.agropay.core.auth.model.element.*;
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
public interface IElementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "container", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    ElementEntity toEntity(CreateElementRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "container", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(UpdateElementRequest request, @MappingTarget ElementEntity entity);

    @Mapping(source = "container.publicId", target = "containerPublicId")
    CommandElementResponse toResponse(ElementEntity entity);

    @Mapping(source = "container.publicId", target = "container.publicId")
    @Mapping(source = "container.name", target = "container.name")
    @Mapping(source = "container.displayName", target = "container.displayName")
    ElementDetailsDTO toDetailsDTO(ElementEntity entity);

    @Mapping(source = "container.publicId", target = "container.publicId")
    @Mapping(source = "container.name", target = "container.name")
    @Mapping(source = "container.displayName", target = "container.displayName")
    ElementListDTO toListDTO(ElementEntity entity);

    List<ElementListDTO> toListDTOs(List<ElementEntity> entities);

    default PagedResult<ElementListDTO> toPagedDTO(Page<ElementEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}

