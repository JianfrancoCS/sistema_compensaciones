package com.agropay.core.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event fired when an attendance marking is created.
 * This event can be used for asynchronous processing like:
 * - Notifications
 * - Analytics
 * - Integration with external systems
 * - Audit logging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMarkedEvent {

    /**
     * Public ID of the marking detail
     */
    private UUID markingDetailPublicId;

    /**
     * Document number of the person who marked attendance
     */
    private String personDocumentNumber;

    /**
     * Name of the person who marked attendance
     */
    private String personFullName;

    /**
     * Whether this is an employee or external person
     */
    private Boolean isEmployee;

    /**
     * Whether this is an entry (true) or exit (false) marking
     */
    private Boolean isEntry;

    /**
     * When the marking was made
     */
    private LocalDateTime markedAt;

    /**
     * Reason for the marking (WORK, VISIT, etc.)
     */
    private String markingReasonCode;

    /**
     * Name of the marking reason
     */
    private String markingReasonName;

    /**
     * Subsidiary where the marking was made
     */
    private UUID subsidiaryPublicId;

    /**
     * Name of the subsidiary
     */
    private String subsidiaryName;

    /**
     * Get a description of the marking type for logging/notifications
     */
    public String getMarkingTypeDescription() {
        String personType = isEmployee ? "Employee" : "External Person";
        String entryType = isEntry ? "Entry" : "Exit";
        return String.format("%s %s", personType, entryType);
    }

    /**
     * Get a summary message for this attendance marking
     */
    public String getSummaryMessage() {
        return String.format("%s (%s) marked %s at %s - %s",
                personFullName,
                personDocumentNumber,
                isEntry ? "entry" : "exit",
                subsidiaryName,
                markingReasonName);
    }
}