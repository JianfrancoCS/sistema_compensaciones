package com.agropay.core.attendance.mapper;

import com.agropay.core.attendance.domain.MarkingReasonEntity;
import com.agropay.core.attendance.model.markingreason.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IMarkingReasonMapper {

    CommandMarkingReasonResponse toCommandResponse(MarkingReasonEntity entity);

    MarkingReasonListDTO toListDTO(MarkingReasonEntity entity);

    MarkingReasonSelectOptionDTO toSelectOptionDTO(MarkingReasonEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isInternal", ignore = true)
    void updateEntityFromRequest(UpdateMarkingReasonRequest request, @MappingTarget MarkingReasonEntity entity);
}