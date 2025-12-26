package com.agropay.core.payroll.service;

import com.agropay.core.payroll.domain.CalendarEventEntity;
import com.agropay.core.payroll.domain.WorkCalendarEntity;
import com.agropay.core.payroll.persistence.ICalendarEventRepository;
import com.agropay.core.payroll.persistence.IWorkCalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing working days and calendar events.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingDaysService {

    private final IWorkCalendarRepository workCalendarRepository;
    private final ICalendarEventRepository calendarEventRepository;

    /**
     * Get all working days for a payroll period.
     *
     * @param periodStart Start date of the period (inclusive).
     * @param periodEnd End date of the period (inclusive).
     * @return List of working days in the period.
     */
    public List<LocalDate> getWorkingDays(LocalDate periodStart, LocalDate periodEnd) {
        return workCalendarRepository.findWorkingDaysBetween(periodStart, periodEnd)
            .stream()
            .map(WorkCalendarEntity::getDate)
            .toList();
    }

    /**
     * Count working days for a payroll period.
     *
     * @param periodStart Start date of the period (inclusive).
     * @param periodEnd End date of the period (inclusive).
     * @return Number of working days.
     */
    public int countWorkingDays(LocalDate periodStart, LocalDate periodEnd) {
        Long count = workCalendarRepository.countWorkingDaysBetween(periodStart, periodEnd);
        return count != null ? count.intValue() : 0;
    }

    /**
     * Get full work calendar entities for a period, including non-working days.
     *
     * @param periodStart Start date of the period (inclusive).
     * @param periodEnd End date of the period (inclusive).
     * @return List of all work calendar entities in the period.
     */
    public List<WorkCalendarEntity> getWorkCalendarEntries(LocalDate periodStart, LocalDate periodEnd) {
        return workCalendarRepository.findAllBetweenWithEvents(periodStart, periodEnd);
    }

    /**
     * Get all calendar events (like holidays) for a period.
     *
     * @param periodStart Start date of the period (inclusive).
     * @param periodEnd End date of the period (inclusive).
     * @return List of calendar events in the period.
     */
    public List<CalendarEventEntity> getCalendarEvents(LocalDate periodStart, LocalDate periodEnd) {
        return calendarEventRepository.findByDateRange(periodStart, periodEnd);
    }
}
