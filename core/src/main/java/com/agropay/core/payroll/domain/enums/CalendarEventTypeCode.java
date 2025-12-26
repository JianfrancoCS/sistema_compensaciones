package com.agropay.core.payroll.domain.enums;

public enum CalendarEventTypeCode {
    HOLIDAY,
    INTERNAL_EVENT,
    NON_WORKING_DAY;

    public static CalendarEventTypeCode fromCode(String code) {
        try {
            return CalendarEventTypeCode.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type code: " + code);
        }
    }
}
