package com.agropay.core.attendance.mapper;

import com.agropay.core.attendance.domain.MarkingDetailEntity;
import com.agropay.core.attendance.model.attendance.AttendanceListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for converting attendance entities to DTOs.
 * Simplified version that handles basic mapping, with complex logic in service layer.
 */
@Mapper(componentModel = "spring")
public interface IAttendanceMapper {

    /**
     * Convert a MarkingDetailEntity to AttendanceListDTO
     * Basic mapping - complex logic handled in service layer
     */
    @Mapping(target = "personDocumentNumber", source = "personDocumentNumber")
    @Mapping(target = "personFullName", ignore = true)
    @Mapping(target = "markingDate", source = "marking.markingDate")
    @Mapping(target = "subsidiaryName", source = "marking.subsidiary.name")
    @Mapping(target = "entryTime", ignore = true)
    @Mapping(target = "exitTime", ignore = true)
    @Mapping(target = "markingReasonName", source = "markingReason.name")
    @Mapping(target = "isEmployee", source = "isEmployee")
    @Mapping(target = "publicId", source = "publicId")
    AttendanceListDTO toAttendanceListDTO(MarkingDetailEntity markingDetail);

    /**
     * Convert a list of marking details to attendance list DTOs
     * @param markingDetails List of marking detail entities
     * @return List of attendance DTOs
     */
    List<AttendanceListDTO> toAttendanceListDTO(List<MarkingDetailEntity> markingDetails);
}