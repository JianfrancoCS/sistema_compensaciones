package com.agropay.core.payroll.mapper;

import com.agropay.core.payroll.domain.CalendarEventEntity;
import com.agropay.core.payroll.domain.WorkCalendarEntity;
import com.agropay.core.payroll.model.calendar.CalendarDayDetailDTO;
import com.agropay.core.payroll.model.calendar.CalendarDayListDTO;
import com.agropay.core.payroll.model.calendar.CommandCalendarEventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface CalendarMapper {

    @Mapping(target = "eventCount", source = "events", qualifiedByName = "countEvents")
    CalendarDayListDTO toCalendarDayListDTO(WorkCalendarEntity entity);

    @Mapping(target = "eventTypePublicId", source = "eventType.publicId")
    @Mapping(target = "eventTypeName", source = "eventType.name")
    CommandCalendarEventResponse toCommandCalendarEventResponse(CalendarEventEntity entity);

    @Mapping(target = "events", source = "events")
    CalendarDayDetailDTO toCalendarDayDetailDTO(WorkCalendarEntity entity);

    @Named("countEvents")
    default int countEvents(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }
}
