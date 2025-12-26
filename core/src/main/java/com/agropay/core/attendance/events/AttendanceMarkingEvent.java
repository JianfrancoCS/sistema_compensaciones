package com.agropay.core.attendance.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class AttendanceMarkingEvent extends ApplicationEvent {

    private final UUID subsidiaryPublicId;
    private final LocalDate markingDate;
    private final String personDocumentNumber;
    private final Boolean isEmployee;
    private final Boolean isEntry;

    public AttendanceMarkingEvent(
            Object source,
            UUID subsidiaryPublicId,
            LocalDate markingDate,
            String personDocumentNumber,
            Boolean isEmployee,
            Boolean isEntry
    ) {
        super(source);
        this.subsidiaryPublicId = subsidiaryPublicId;
        this.markingDate = markingDate;
        this.personDocumentNumber = personDocumentNumber;
        this.isEmployee = isEmployee;
        this.isEntry = isEntry;
    }
}