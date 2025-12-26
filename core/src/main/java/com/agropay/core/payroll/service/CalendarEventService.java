package com.agropay.core.payroll.service;

import com.agropay.core.payroll.service.usecase.ICalendarEventService;
import com.agropay.core.payroll.mapper.CalendarMapper;
import com.agropay.core.payroll.domain.CalendarEventEntity;
import com.agropay.core.payroll.domain.CalendarEventTypeEntity;
import com.agropay.core.payroll.domain.WorkCalendarEntity;
import com.agropay.core.payroll.model.calendar.AvailablePeriodDTO;
import com.agropay.core.payroll.model.calendar.CalendarDayDetailDTO;
import com.agropay.core.payroll.model.calendar.CalendarDayListDTO;
import com.agropay.core.payroll.model.calendar.CalendarEventTypeSelectOptionDTO;
import com.agropay.core.payroll.model.calendar.CommandCalendarEventResponse;
import com.agropay.core.payroll.model.calendar.CreateCalendarEventRequest;
import com.agropay.core.payroll.model.calendar.MonthInfoDTO;
import com.agropay.core.payroll.model.calendar.UpdateWorkingDayRequest;
import com.agropay.core.payroll.persistence.ICalendarEventRepository;
import com.agropay.core.payroll.persistence.ICalendarEventTypeRepository;
import com.agropay.core.payroll.persistence.IWorkCalendarRepository;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashSet; 

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarEventService implements ICalendarEventService {

    private final IWorkCalendarRepository workCalendarRepository;
    private final ICalendarEventRepository calendarEventRepository;
    private final ICalendarEventTypeRepository calendarEventTypeRepository;
    private final CalendarMapper calendarMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventTypeSelectOptionDTO> getEventTypes() {
        return calendarEventTypeRepository.findAll().stream()
            .map(eventType -> new CalendarEventTypeSelectOptionDTO(
                eventType.getPublicId(),
                eventType.getName()
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailablePeriodDTO> getAvailablePeriods() {
        List<Object[]> results = workCalendarRepository.findDistinctYearMonthCombinations();

        // Agrupar por año
        Map<Integer, List<MonthInfoDTO>> periodsByYear = results.stream()
            .collect(Collectors.groupingBy(
                result -> (Integer) result[0], // year
                Collectors.mapping(
                    result -> {
                        int month = (Integer) result[1];
                        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                        // Capitalizar primera letra
                        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
                        return new MonthInfoDTO(month, monthName);
                    },
                    Collectors.toList()
                )
            ));

        // Convertir a lista de AvailablePeriodDTO ordenada por año DESC
        return periodsByYear.entrySet().stream()
            .sorted(Map.Entry.<Integer, List<MonthInfoDTO>>comparingByKey().reversed())
            .map(entry -> new AvailablePeriodDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarDayListDTO> getDaysInMonth(int year, int month) {
        // Validar que el período solicitado exista
        List<AvailablePeriodDTO> availablePeriods = getAvailablePeriods();
        boolean periodExists = availablePeriods.stream()
            .anyMatch(period -> period.year() == year &&
                period.months().stream().anyMatch(m -> m.month() == month));

        if (!periodExists) {
            throw new BusinessValidationException(
                "exception.calendar.period.not-found",
                new String[]{String.valueOf(year), String.valueOf(month)}
            );
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return getDaysInRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarDayListDTO> getDaysInRange(LocalDate startDate, LocalDate endDate) {
        List<WorkCalendarEntity> calendarDays = workCalendarRepository.findAllBetweenWithEvents(startDate, endDate);

        // Generar todos los días en el rango, incluso los que no están en la BD
        return startDate.datesUntil(endDate.plusDays(1))
            .map(date -> {
                WorkCalendarEntity dayEntity = calendarDays.stream()
                    .filter(d -> d.getDate().equals(date))
                    .findFirst()
                    .orElse(null);

                if (dayEntity != null) {
                    return calendarMapper.toCalendarDayListDTO(dayEntity);
                } else {
                    // Crear DTO para día que no existe en BD (asume laborable excepto domingos)
                    boolean isWorkingDay = date.getDayOfWeek() != DayOfWeek.SUNDAY;
                    return new CalendarDayListDTO(null, date, isWorkingDay, 0);
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandCalendarEventResponse> getEventsForDay(UUID dayPublicId) {
        WorkCalendarEntity workDay = workCalendarRepository.findByPublicId(dayPublicId)
            .orElseThrow(() -> new EntityNotFoundException("Calendar day not found with publicId: " + dayPublicId));

        return workDay.getEvents().stream()
            .map(calendarMapper::toCommandCalendarEventResponse)
            .collect(Collectors.toList());
    }

    @Override
    public CommandCalendarEventResponse createEventForDay(CreateCalendarEventRequest request) {
        CalendarEventTypeEntity eventType = calendarEventTypeRepository.findByPublicId(request.eventTypePublicId())
            .orElseThrow(() -> new IdentifierNotFoundException("exception.shared.identifier-not-found", request.eventTypePublicId()));

        WorkCalendarEntity workDay = workCalendarRepository.findByDate(request.date())
            .orElseGet(() -> createWorkDay(request.date()));

        CalendarEventEntity newEvent = CalendarEventEntity.builder()
            .publicId(UUID.randomUUID())
            .workCalendar(workDay)
            .eventType(eventType)
            .description(request.description())
            .build();

        calendarEventRepository.save(newEvent);

        return calendarMapper.toCommandCalendarEventResponse(newEvent);
    }

    @Override
    public void deleteEvent(UUID eventPublicId) {
        CalendarEventEntity eventToDelete = calendarEventRepository.findByPublicId(eventPublicId)
            .orElseThrow(() -> new EntityNotFoundException("Calendar event not found with publicId: " + eventPublicId));
        calendarEventRepository.softDelete(eventToDelete.getId(), "SYSTEM");
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarDayDetailDTO getDayDetail(LocalDate date) {
        WorkCalendarEntity workDay = workCalendarRepository.findByDate(date)
            .orElseGet(() -> {
                // If not found, create a transient entity for the DTO
                boolean isWorkingDay = date.getDayOfWeek() != DayOfWeek.SUNDAY;
                return WorkCalendarEntity.builder()
                    .publicId(null) // No publicId as it's not persisted
                    .date(date)
                    .isWorkingDay(isWorkingDay)
                    .events(new HashSet<>()) // Empty set of events
                    .build();
            });
        return calendarMapper.toCalendarDayDetailDTO(workDay);
    }

    @Override
    public CommandCalendarEventResponse updateWorkingDay(UpdateWorkingDayRequest request) {
        WorkCalendarEntity workDay = workCalendarRepository.findByDate(request.date())
            .orElseGet(() -> createWorkDay(request.date())); // Create if not exists

        workDay.setIsWorkingDay(request.isWorkingDay()); // Corrected setter call
        workCalendarRepository.save(workDay);

        // Return a CommandCalendarEventResponse using its constructor
        return new CommandCalendarEventResponse(
            workDay.getPublicId(),
            "Working day status updated to " + request.isWorkingDay(),
            null, // eventTypePublicId is null as this is not an event creation
            null  // eventTypeName is null as this is not an event creation
        );
    }

    private WorkCalendarEntity createWorkDay(LocalDate date) {
        boolean isWorkingDay = !(date.getDayOfWeek() == DayOfWeek.SUNDAY);
        WorkCalendarEntity newWorkDay = WorkCalendarEntity.builder()
            .publicId(UUID.randomUUID())
            .date(date)
            .isWorkingDay(isWorkingDay)
            .build();
        return workCalendarRepository.save(newWorkDay);
    }
}
