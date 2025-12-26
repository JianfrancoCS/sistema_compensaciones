import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { PeriodService } from '../services/period.service';
import { MessageService } from 'primeng/api';
import { PeriodDTO, CreatePeriodRequest, PeriodState, PeriodSelectOption } from '@shared/types/period';
import { ApiResult } from '@shared/types/api';

export type { PeriodDTO, CreatePeriodRequest, PeriodSelectOption };

interface PeriodStoreState extends PeriodState {
  selectOptions: PeriodSelectOption[];
  selectOptionsLoading: boolean;
}

const initialState: PeriodStoreState = {
  periods: [],
  loading: false,
  error: null,
  selectOptions: [],
  selectOptionsLoading: false
};

export const PeriodStore = signalStore(
  withState<PeriodStoreState>(initialState),

  withProps(() => ({
    _periodService: inject(PeriodService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.periods().length === 0)
  })),

  withMethods((store) => {
    const showMessage = (message: string, severity: 'success' | 'error') => {
      store._messageService.add({
        severity,
        summary: severity === 'success' ? 'Éxito' : 'Error',
        detail: message
      });
    };

    const handleHttpError = (err: any, defaultMessage: string, apiResponseMessage?: string) => {
      let messages: string[] = [];

      if (apiResponseMessage) {
        messages.push(apiResponseMessage);
      } else if (err?.error?.message) {
        if (Array.isArray(err.error.message)) {
          messages = err.error.message;
        } else {
          messages.push(err.error.message);
        }
      } else if (err?.message) {
        messages.push(err.message);
      } else {
        messages.push(defaultMessage);
      }

      messages.forEach(msg => showMessage(msg, 'error'));

      const errorForState = messages.join('\n');
      patchState(store, { loading: false, error: errorForState });
    };

    const loadPeriods = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          store._periodService.getAll().pipe(
            tap((response: ApiResult<PeriodDTO[]>) => {
              if (response.success) {
                patchState(store, {
                  periods: response.data,
                  loading: false
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar períodos', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar períodos');
              return of(null);
            })
          )
        )
      )
    );

    const create = rxMethod<CreatePeriodRequest>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((request) =>
          store._periodService.create(request).pipe(
            tap((response: ApiResult<PeriodDTO>) => {
              if (response.success) {
                patchState(store, { loading: false });
                showMessage(response.message, 'success');
                loadPeriods();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al crear período', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al crear período');
              return of(null);
            })
          )
        )
      )
    );

    const deletePeriod = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((publicId) =>
          store._periodService.delete(publicId).pipe(
            tap((response: ApiResult<void>) => {
              if (response.success) {
                patchState(store, { loading: false });
                showMessage(response.message, 'success');
                loadPeriods();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al eliminar período', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al eliminar período');
              return of(null);
            })
          )
        )
      )
    );

    const loadSelectOptions = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { selectOptionsLoading: true })),
        switchMap(() =>
          store._periodService.getSelectOptions().pipe(
            tap((response: ApiResult<PeriodSelectOption[]>) => {
              if (response.success) {
                patchState(store, {
                  selectOptions: response.data,
                  selectOptionsLoading: false
                });
              } else {
                patchState(store, { selectOptionsLoading: false });
                handleHttpError(null, 'Error al cargar opciones de períodos', response.message);
              }
            }),
            catchError((err) => {
              patchState(store, { selectOptionsLoading: false });
              handleHttpError(err, 'Error al cargar opciones de períodos');
              return of(null);
            })
          )
        )
      )
    );

    return {
      loadPeriods,
      loadSelectOptions,
      create,
      delete: deletePeriod,
      clearError: () => patchState(store, { error: null }),
      init: () => {
        loadPeriods();
      }
    };
  })
);