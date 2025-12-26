package com.agropay.core.payroll.service.usecase;

import com.agropay.core.payroll.model.calendar.AvailablePeriodDTO;
import com.agropay.core.payroll.model.calendar.CalendarDayDetailDTO;
import com.agropay.core.payroll.model.calendar.CalendarDayListDTO;
import com.agropay.core.payroll.model.calendar.CalendarEventTypeSelectOptionDTO;
import com.agropay.core.payroll.model.calendar.CommandCalendarEventResponse;
import com.agropay.core.payroll.model.calendar.CreateCalendarEventRequest;
import com.agropay.core.payroll.model.calendar.UpdateWorkingDayRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ICalendarEventService {

    /**
     * Gets all available event types for calendar events.
     *
     * @return A list of event types as select options.
     */
    List<CalendarEventTypeSelectOptionDTO> getEventTypes();

    /**
     * Gets all available year-month periods that have calendar entries.
     *
     * @return A list of available periods with year, month number, and month name.
     */
    List<AvailablePeriodDTO> getAvailablePeriods();

    /**
     * Finds all calendar days for a specific month.
     *
     * @param year The year.
     * @param month The month (1-12).
     * @return A list of calendar days for that month with event count.
     */
    List<CalendarDayListDTO> getDaysInMonth(int year, int month);

    /**
     * Finds all calendar days within a given date range.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of calendar days with a flag indicating if they have events.
     */
    List<CalendarDayListDTO> getDaysInRange(LocalDate startDate, LocalDate endDate);

    /**
     * Finds all events for a specific calendar day.
     *
     * @param dayPublicId The public ID of the calendar day.
     * @return A list of event details for that day.
     */
    List<CommandCalendarEventResponse> getEventsForDay(UUID dayPublicId);

    /**
     * Creates a new event for a specific day.
     *
     * @param request The request containing the date, event type, and description.
     * @return The details of the created event.
     */
    CommandCalendarEventResponse createEventForDay(CreateCalendarEventRequest request);

    /**
     * Deletes a calendar event by its public ID.
     *
     * @param eventPublicId The public ID of the event to delete.
     */
    void deleteEvent(UUID eventPublicId);

    /**
     * Gets the details of a specific calendar day, including its working day status.
     *
     * @param date The date of the day to retrieve.
     * @return The details of the calendar day.
     */
    CalendarDayDetailDTO getDayDetail(LocalDate date);

    /**
     * Updates the working day status of a specific calendar day.
     *
     * @param request The request containing the date and the new working day status.
     * @return The details of the updated calendar event.
     */
    CommandCalendarEventResponse updateWorkingDay(UpdateWorkingDayRequest request);
}
