package com.agropay.core.assignment.mapper;

import com.agropay.core.assignment.domain.LoteEntity;
import com.agropay.core.assignment.model.lote.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ILoteMapper {

    LoteEntity toEntity(CreateLoteRequest request);

    void updateEntityFromRequest(UpdateLoteRequest request, @MappingTarget LoteEntity entity);

    @Mapping(target = "subsidiaryPublicId", source = "subsidiary.publicId")
    CommandLoteResponse toResponse(LoteEntity entity);

    @Mapping(target = "subsidiaryName", source = "subsidiary.name")
    LoteListDTO toListDTO(LoteEntity entity);

    List<LoteListDTO> toListDTOs(List<LoteEntity> entities);

    default PagedResult<LoteListDTO> toPagedDTO(Page<LoteEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }

    @Mapping(target = "id", source = "publicId")
    @Mapping(target = "subsidiaryId", source = "subsidiary.publicId")
    LoteSyncResponse toSyncResponse(LoteEntity entity);

    List<LoteSyncResponse> toSyncResponses(List<LoteEntity> entities);
}