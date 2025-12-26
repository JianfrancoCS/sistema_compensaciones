import { inject } from '@angular/core';
import { signalStore, withState, withMethods, patchState, withProps } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { CalendarService } from '../services/calendar.service';
import {
  CalendarState,
  AvailablePeriodDTO,
  CalendarDayListDTO,
  CreateCalendarEventRequest,
  CommandCalendarEventResponse,
  EventTypeSelectOptionDTO,
  UpdateWorkingDayRequest
} from '@shared/types/calendar';
import { ApiResult } from '@shared/types/api';
import { MessageService } from 'primeng/api';

const initialState: CalendarState = {
  availablePeriods: [],
  days: [],
  selectedPeriod: null,
  selectedDay: null,
  dayEvents: [],
  eventTypeOptions: [],
  loading: false,
  loadingEvents: false,
  error: null,
  workingDayUpdated: false, // Nueva propiedad de estado
};

export const CalendarStore = signalStore(
  withState<CalendarState>(initialState),

  withProps(() => ({
    _calendarService: inject(CalendarService),
    _messageService: inject(MessageService),
  })),

  withMethods((store) => {
    const handleHttpError = (err: any, defaultMessage: string) => {
      const message = err?.error?.message || err?.message || defaultMessage;
      store._messageService.add({ severity: 'error', summary: 'Error', detail: message });
      patchState(store, { loading: false, loadingEvents: false, error: message });
    };

    const loadCalendarDays = rxMethod<{ year: number; month: number }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null, days: [] })),
        switchMap(({ year, month }) =>
          store._calendarService.getCalendarDays(year, month).pipe(
            tap((response: ApiResult<CalendarDayListDTO[]>) => {
              if (response.success) {
                patchState(store, { days: response.data, loading: false });
              } else {
                handleHttpError(null, 'Error al cargar los días del calendario');
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar días');
              return of(null);
            })
          )
        )
      )
    );

    const selectPeriod = (period: { year: number; month: number }) => {
      patchState(store, { selectedPeriod: period });
      loadCalendarDays(period);
    };

    const loadAvailablePeriods = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          store._calendarService.getAvailablePeriods().pipe(
            tap((response: ApiResult<AvailablePeriodDTO[]>) => {
              if (response.success && response.data.length > 0) {
                patchState(store, { availablePeriods: response.data });
                const latestYear = response.data[response.data.length - 1];
                const latestMonth = latestYear.months[latestYear.months.length - 1];
                selectPeriod({ year: latestYear.year, month: latestMonth.month });
              } else {
                patchState(store, { loading: false });
                if (!response.success) handleHttpError(null, 'Error al cargar los períodos disponibles');
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar períodos');
              return of(null);
            })
          )
        )
      )
    );

    const loadEventTypes = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._calendarService.getEventTypes().pipe(
            tap((response: ApiResult<EventTypeSelectOptionDTO[]>) => {
              if (response.success) {
                patchState(store, { eventTypeOptions: response.data });
              } else {
                handleHttpError(null, 'Error al cargar tipos de evento');
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar tipos de evento');
              return of(null);
            })
          )
        )
      )
    );

    const loadDayEvents = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loadingEvents: true, dayEvents: [] })),
        switchMap((dayPublicId) =>
          store._calendarService.getEventsForDay(dayPublicId).pipe(
            tap((response: ApiResult<CommandCalendarEventResponse[]>) => {
              if (response.success) {
                patchState(store, { dayEvents: response.data, loadingEvents: false });
              } else {
                handleHttpError(null, 'Error al cargar eventos del día');
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar eventos');
              patchState(store, { loadingEvents: false });
              return of(null);
            })
          )
        )
      )
    );

    const createEvent = rxMethod<CreateCalendarEventRequest>(
      pipe(
        tap(() => patchState(store, { loadingEvents: true })),
        switchMap((request) =>
          store._calendarService.createEvent(request).pipe(
            tap((response) => {
              if (response.success) {
                store._messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Evento creado.' });
                patchState(store, { loadingEvents: false }); // Set loadingEvents to false BEFORE reloading
                loadDayEvents(store.selectedDay()!.publicId);
                loadCalendarDays(store.selectedPeriod()!);
              } else {
                handleHttpError(null, 'Error al crear el evento');
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al crear el evento');
              patchState(store, { loadingEvents: false });
              return of(null);
            })
          )
        )
      )
    );

    const deleteEvent = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loadingEvents: true })),
        switchMap((eventPublicId) =>
          store._calendarService.deleteEvent(eventPublicId).pipe(
            tap(() => {
              store._messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Evento eliminado.' });
              patchState(store, { loadingEvents: false }); // Set loadingEvents to false BEFORE reloading
              loadDayEvents(store.selectedDay()!.publicId);
              loadCalendarDays(store.selectedPeriod()!);
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al eliminar el evento');
              patchState(store, { loadingEvents: false });
              return of(null);
            })
          )
        )
      )
    );

    const updateDayWorkingStatus = rxMethod<UpdateWorkingDayRequest>(
      pipe(
        tap(() => patchState(store, { loadingEvents: true })),
        switchMap((request) =>
          store._calendarService.updateDayWorkingStatus(request).pipe(
            tap(() => {
              store._messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Estado del día actualizado.' });
              patchState(store, { loadingEvents: false, workingDayUpdated: true });
              loadCalendarDays(store.selectedPeriod()!); // Reload the main calendar grid
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al actualizar el estado del día');
              patchState(store, { loadingEvents: false });
              return of(null);
            })
          )
        )
      )
    );

    return {
      selectPeriod,
      setSelectedDay: (day: CalendarDayListDTO) => {
        patchState(store, { selectedDay: day });
        loadDayEvents(day.publicId);
      },
      createEvent,
      deleteEvent,
      updateDayWorkingStatus,
      clearSelectedDayAndEvents: () => {
        patchState(store, { selectedDay: null, dayEvents: [] });
      },
      clearError: () => {
        patchState(store, { error: null });
      },
      resetWorkingDayUpdated: () => {
        patchState(store, { workingDayUpdated: false });
      },
      init: () => {
        loadAvailablePeriods();
        loadEventTypes();
      },
    };
  })
);
