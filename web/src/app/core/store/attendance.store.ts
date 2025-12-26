import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { SelectOption } from '@shared/types/api';
import {
  ExternalMarkRequest,
  EmployeeMarkRequest,
  MarkingResponse,
  ApiMarkingResponse
} from '@shared/types/attendance';
import { AttendanceService } from '../services/attendance.service';

export type {
  ExternalMarkRequest,
  EmployeeMarkRequest,
  MarkingResponse,
  ApiMarkingResponse
};

interface AttendanceState {
  loading: boolean;
  error: string | null;
  lastMarkingResponse: MarkingResponse | null;
  markingReasons: SelectOption[];
  externalMarkingReasons: SelectOption[];
}

const initialState: AttendanceState = {
  loading: false,
  error: null,
  lastMarkingResponse: null,
  markingReasons: [],
  externalMarkingReasons: []
};

export const AttendanceStore = signalStore(
  withState<AttendanceState>(initialState),

  withProps(() => ({
    _attendanceService: inject(AttendanceService)
  })),

  withComputed((state) => ({
    hasLastMarking: computed(() => state.lastMarkingResponse() !== null)
  })),

  withMethods((store) => {
    const getErrorMessage = (err: any, defaultMessage: string): string => {
      if (err?.error && typeof err.error === 'object' && 'message' in err.error) {
        return err.error.message;
      }
      return err?.message || defaultMessage;
    };

    const loadMarkingReasons = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._attendanceService.getEmployeeMarkingReasonsSelectOptions().pipe(
            tap((response) => {
              patchState(store, {
                markingReasons: response.success ? response.data : []
              });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar razones de marcado de empleados');
              patchState(store, { error });
              return of(null);
            })
          )
        )
      )
    );

    const loadExternalMarkingReasons = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._attendanceService.getExternalMarkingReasonsSelectOptions().pipe(
            tap((response) => {
              patchState(store, {
                externalMarkingReasons: response.success ? response.data : []
              });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar razones de marcado externo');
              patchState(store, { error });
              return of(null);
            })
          )
        )
      )
    );

    return {
      markEmployee: (request: EmployeeMarkRequest) => {
        patchState(store, { loading: true, error: null, lastMarkingResponse: null });

        return store._attendanceService.markEmployee(request).pipe(
          tap((response) => {
            if (response.success) {
              patchState(store, {
                loading: false,
                lastMarkingResponse: response.data
              });
            } else {
              patchState(store, {
                loading: false,
                error: response.message
              });
            }
          }),
          catchError((err) => {
            const error = getErrorMessage(err, 'Error al marcar asistencia del empleado');
            patchState(store, { loading: false, error });
            return of(null);
          })
        );
      },

      markExternal: (request: ExternalMarkRequest) => {
        patchState(store, { loading: true, error: null, lastMarkingResponse: null });

        return store._attendanceService.markExternal(request).pipe(
          tap((response) => {
            if (response.success) {
              patchState(store, {
                loading: false,
                lastMarkingResponse: response.data
              });
            } else {
              patchState(store, {
                loading: false,
                error: response.message
              });
            }
          }),
          catchError((err) => {
            const error = getErrorMessage(err, 'Error al marcar asistencia externa');
            patchState(store, { loading: false, error });
            return of(null);
          })
        );
      },


      clearLastMarking: () => {
        patchState(store, { lastMarkingResponse: null });
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      loadSelectOptions: () => {
        loadMarkingReasons();
        loadExternalMarkingReasons();
      },

      init: () => {
        loadMarkingReasons();
        loadExternalMarkingReasons();
      }
    };
  })
);