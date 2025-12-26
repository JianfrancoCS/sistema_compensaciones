package com.agropay.core.attendance.listener;

import com.agropay.core.shared.events.AttendanceMarkedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for attendance-related events.
 * Handles asynchronous processing of attendance markings for:
 * - Logging and audit trails
 * - Notifications
 * - Analytics
 * - Integration with external systems
 */
@Slf4j
@Component
public class AttendanceEventListener {

    /**
     * Handle attendance marked events asynchronously
     * @param event The attendance marked event
     */
    @EventListener
    @Async
    public void handleAttendanceMarked(AttendanceMarkedEvent event) {
        try {
            log.info("Processing attendance marking: {}", event.getSummaryMessage());

            // Process the event based on marking type
            if (event.getIsEmployee()) {
                handleEmployeeAttendance(event);
            } else {
                handleExternalPersonAttendance(event);
            }

            // Additional processing could include:
            // - Send notifications to supervisors
            // - Update attendance statistics
            // - Trigger integrations with external systems
            // - Generate alerts for unusual patterns

        } catch (Exception e) {
            log.error("Error processing attendance event for person: {}",
                     event.getPersonDocumentNumber(), e);
        }
    }

    /**
     * Handle employee-specific attendance processing
     * @param event The attendance event for an employee
     */
    private void handleEmployeeAttendance(AttendanceMarkedEvent event) {
        log.debug("Processing employee attendance: {} - {}",
                 event.getPersonDocumentNumber(),
                 event.getMarkingTypeDescription());

        // Employee-specific processing:
        // - Check for tardiness
        // - Calculate overtime
        // - Update employee attendance statistics
        // - Trigger payroll calculations

        if (event.getIsEntry()) {
            log.debug("Employee {} started work at {}",
                     event.getPersonDocumentNumber(),
                     event.getMarkedAt());
        } else {
            log.debug("Employee {} finished work at {}",
                     event.getPersonDocumentNumber(),
                     event.getMarkedAt());
        }
    }

    /**
     * Handle external person attendance processing
     * @param event The attendance event for an external person
     */
    private void handleExternalPersonAttendance(AttendanceMarkedEvent event) {
        log.debug("Processing external person attendance: {} - {}",
                 event.getPersonDocumentNumber(),
                 event.getMarkingTypeDescription());

        // External person-specific processing:
        // - Security notifications
        // - Visitor logs
        // - Access control updates
        // - Contractor time tracking

        log.info("External person {} marked {} for {} at {}",
                event.getPersonFullName(),
                event.getIsEntry() ? "entry" : "exit",
                event.getMarkingReasonName(),
                event.getSubsidiaryName());
    }
}