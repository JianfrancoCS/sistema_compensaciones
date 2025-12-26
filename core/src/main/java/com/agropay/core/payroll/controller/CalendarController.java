package com.agropay.core.payroll.controller;

import com.agropay.core.payroll.service.usecase.ICalendarEventService;
import com.agropay.core.payroll.model.calendar.AvailablePeriodDTO;
import com.agropay.core.payroll.model.calendar.CalendarDayDetailDTO;
import com.agropay.core.payroll.model.calendar.CalendarDayListDTO;
import com.agropay.core.payroll.model.calendar.CalendarEventTypeSelectOptionDTO;
import com.agropay.core.payroll.model.calendar.CommandCalendarEventResponse;
import com.agropay.core.payroll.model.calendar.CreateCalendarEventRequest;
import com.agropay.core.payroll.model.calendar.UpdateWorkingDayRequest;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/calendar")
@RequiredArgsConstructor
@Tag(name = "Work Calendar", description = "Gestión del calendario laboral y eventos")
public class CalendarController {

    private final ICalendarEventService calendarEventService;

    @Operation(summary = "Listar tipos de eventos", description = "Obtiene todos los tipos de eventos disponibles para crear eventos en el calendario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tipos de eventos obtenidos exitosamente")
    })
    @GetMapping("/event-types")
    public ResponseEntity<ApiResult<List<CalendarEventTypeSelectOptionDTO>>> getEventTypes() {
        List<CalendarEventTypeSelectOptionDTO> eventTypes = calendarEventService.getEventTypes();
        return ResponseEntity.ok(ApiResult.success(eventTypes));
    }

    @Operation(summary = "Obtener períodos disponibles", description = "Lista todos los años y meses que tienen datos en el calendario laboral. Usar esta lista antes de solicitar días de un mes específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Períodos obtenidos exitosamente")
    })
    @GetMapping("/available-periods")
    public ResponseEntity<ApiResult<List<AvailablePeriodDTO>>> getAvailablePeriods() {
        List<AvailablePeriodDTO> periods = calendarEventService.getAvailablePeriods();
        return ResponseEntity.ok(ApiResult.success(periods));
    }

    @Operation(summary = "Listar días del mes", description = "Obtiene todos los días de un mes con información sobre si es laborable y cantidad de eventos. Validar primero que el año/mes exista en /available-periods.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "400", description = "Año/mes no disponible en el calendario")
    })
    @GetMapping("/days")
    public ResponseEntity<ApiResult<List<CalendarDayListDTO>>> getCalendarDays(
        @Parameter(description = "Año (ej: 2025)", required = true)
        @RequestParam @NotNull @Min(2000) @Max(2100) Integer year,
        @Parameter(description = "Mes (1-12)", required = true)
        @RequestParam @NotNull @Min(1) @Max(12) Integer month
    ) {
        List<CalendarDayListDTO> days = calendarEventService.getDaysInMonth(year, month);
        return ResponseEntity.ok(ApiResult.success(days));
    }

    @Operation(summary = "Ver eventos de un día", description = "Obtiene todos los eventos de un día específico del calendario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Eventos obtenidos exitosamente"),
        @ApiResponse(responseCode = "404", description = "Día no encontrado")
    })
    @GetMapping("/days/{dayPublicId}/events")
    public ResponseEntity<ApiResult<List<CommandCalendarEventResponse>>> getEventsForDay(
        @Parameter(description = "UUID del día", required = true)
        @PathVariable @NotNull UUID dayPublicId
    ) {
        List<CommandCalendarEventResponse> events = calendarEventService.getEventsForDay(dayPublicId);
        return ResponseEntity.ok(ApiResult.success(events));
    }

    @Operation(summary = "Agregar evento a un día", description = "Crea un nuevo evento en el calendario laboral para una fecha específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Tipo de evento no encontrado")
    })
    @PostMapping("/events")
    public ResponseEntity<ApiResult<CommandCalendarEventResponse>> createCalendarEvent(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del evento a crear")
        @RequestBody @Valid CreateCalendarEventRequest request
    ) {
        CommandCalendarEventResponse newEvent = calendarEventService.createEventForDay(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(newEvent));
    }

    @Operation(summary = "Eliminar evento", description = "Elimina (soft delete) un evento del calendario por su UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Evento eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @DeleteMapping("/events/{eventPublicId}")
    public ResponseEntity<ApiResult<Void>> deleteCalendarEvent(
        @Parameter(description = "UUID del evento", required = true)
        @PathVariable @NotNull UUID eventPublicId
    ) {
        calendarEventService.deleteEvent(eventPublicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @Operation(summary = "Obtener detalle de un día", description = "Obtiene el detalle de un día específico, incluyendo si es laborable o no.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle del día obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Día no encontrado")
    })
    @GetMapping("/days/{date}")
    public ResponseEntity<ApiResult<CalendarDayDetailDTO>> getDayDetail(
        @Parameter(description = "Fecha del día (formato YYYY-MM-DD)", required = true)
        @PathVariable @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        CalendarDayDetailDTO dayDetail = calendarEventService.getDayDetail(date);
        return ResponseEntity.ok(ApiResult.success(dayDetail));
    }

    @Operation(summary = "Actualizar día laborable", description = "Actualiza el estado de un día (laborable/no laborable).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado del día actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Día no encontrado")
    })
    @PutMapping("/days/working-day")
    public ResponseEntity<ApiResult<CommandCalendarEventResponse>> updateWorkingDay(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos para actualizar el estado laborable del día")
        @RequestBody @Valid UpdateWorkingDayRequest request
    ) {
        CommandCalendarEventResponse updatedDay = calendarEventService.updateWorkingDay(request);
        return ResponseEntity.ok(ApiResult.success(updatedDay));
    }
}
