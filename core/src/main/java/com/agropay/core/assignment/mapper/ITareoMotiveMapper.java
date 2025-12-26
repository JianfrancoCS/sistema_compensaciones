package com.agropay.core.assignment.mapper;

import com.agropay.core.assignment.domain.TareoMotiveEntity;
import com.agropay.core.assignment.model.tareomotive.*;
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
public interface ITareoMotiveMapper {

    TareoMotiveEntity toEntity(CreateTareoMotiveRequest request);

    void updateEntityFromRequest(UpdateTareoMotiveRequest request, @MappingTarget TareoMotiveEntity entity);

    CommandTareoMotiveResponse toResponse(TareoMotiveEntity entity);

    TareoMotiveListDTO toListDTO(TareoMotiveEntity entity);

    List<TareoMotiveListDTO> toListDTOs(List<TareoMotiveEntity> entities);

    List<TareoMotiveSelectOptionDTO> toSelectOptionDTOs(List<TareoMotiveEntity> entities);

    default PagedResult<TareoMotiveListDTO> toPagedDTO(Page<TareoMotiveEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}