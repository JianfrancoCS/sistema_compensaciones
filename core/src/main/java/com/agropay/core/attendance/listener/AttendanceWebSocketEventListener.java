package com.agropay.core.attendance.listener;

import com.agropay.core.attendance.application.usecase.IAttendanceUseCase;
import com.agropay.core.attendance.events.AttendanceMarkingEvent;
import com.agropay.core.attendance.model.attendance.AttendanceCountSummaryDTO;
import com.agropay.core.attendance.model.websocket.AttendanceCountWebSocketDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceWebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final IAttendanceUseCase attendanceUseCase;

    @Async
    @EventListener
    public void handleGeneralAttendanceCount(AttendanceMarkingEvent event) {
        log.debug("Processing general attendance count for subsidiary: {}", event.getSubsidiaryPublicId());

        try {
            AttendanceCountSummaryDTO generalCount = attendanceUseCase.getFlexibleAttendanceSummary(
                    event.getSubsidiaryPublicId(),
                    event.getMarkingDate(),
                    null // null = ambos tipos
            );

            String subsidiaryId = event.getSubsidiaryPublicId().toString();

            // Topic general: /topic/subsidiary/{subsidiaryId}/count
            messagingTemplate.convertAndSend(
                    "/topic/subsidiary/" + subsidiaryId + "/count",
                    AttendanceCountWebSocketDTO.create(
                            generalCount.subsidiaryPublicId(),
                            generalCount.date(),
                            generalCount.total(),
                            generalCount.outside()
                    )
            );

            log.debug("Successfully published general attendance count to WebSocket for subsidiary: {}", subsidiaryId);

        } catch (Exception e) {
            log.error("Error processing general attendance count for subsidiary: {}",
                    event.getSubsidiaryPublicId(), e);
        }
    }

    @Async
    @EventListener
    public void handleEmployeesAttendanceCount(AttendanceMarkingEvent event) {
        log.debug("Processing employees attendance count for subsidiary: {}", event.getSubsidiaryPublicId());

        try {
            AttendanceCountSummaryDTO employeesCount = attendanceUseCase.getFlexibleAttendanceSummary(
                    event.getSubsidiaryPublicId(),
                    event.getMarkingDate(),
                    false
            );

            String subsidiaryId = event.getSubsidiaryPublicId().toString();

            messagingTemplate.convertAndSend(
                    "/topic/subsidiary/" + subsidiaryId + "/employees/count",
                    AttendanceCountWebSocketDTO.create(
                            employeesCount.subsidiaryPublicId(),
                            employeesCount.date(),
                            employeesCount.total(),
                            employeesCount.outside()
                    )
            );

            log.debug("Successfully published employees attendance count to WebSocket for subsidiary: {}", subsidiaryId);

        } catch (Exception e) {
            log.error("Error processing employees attendance count for subsidiary: {}",
                    event.getSubsidiaryPublicId(), e);
        }
    }

    @Async
    @EventListener
    public void handleExternalsAttendanceCount(AttendanceMarkingEvent event) {
        log.debug("Processing externals attendance count for subsidiary: {}", event.getSubsidiaryPublicId());

        try {
            AttendanceCountSummaryDTO externalsCount = attendanceUseCase.getFlexibleAttendanceSummary(
                    event.getSubsidiaryPublicId(),
                    event.getMarkingDate(),
                    true // true = solo externos
            );

            String subsidiaryId = event.getSubsidiaryPublicId().toString();

            messagingTemplate.convertAndSend(
                    "/topic/subsidiary/" + subsidiaryId + "/externals/count",
                    AttendanceCountWebSocketDTO.create(
                            externalsCount.subsidiaryPublicId(),
                            externalsCount.date(),
                            externalsCount.total(),
                            externalsCount.outside()
                    )
            );

            log.debug("Successfully published externals attendance count to WebSocket for subsidiary: {}", subsidiaryId);

        } catch (Exception e) {
            log.error("Error processing externals attendance count for subsidiary: {}",
                    event.getSubsidiaryPublicId(), e);
        }
    }
}