import { inject, computed } from '@angular/core';
import { patchState, signalStore, withMethods, withState, withProps, withComputed } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { LaborService } from '@core/services/labor.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import {
  LaborFilters,
  LaborState,
  LaborPageableRequest,
  LaborListDTO,
  CreateLaborRequest,
  UpdateLaborRequest
} from '@shared/types/labor';
import { ApiResult, PagedResult } from '@shared/types/api';

export type { LaborListDTO, CreateLaborRequest, UpdateLaborRequest };

const initialState: LaborState = {
  labors: [],
  loading: false,
  error: null,
  totalElements: 0,
  selectOptions: [],
  filters: {
    name: '',
    isPiecework: undefined,
    laborUnitPublicId: undefined,
    page: 0,
    size: 10,
    sortBy: 'name',
    sortDirection: 'asc',
  },
};

export const LaborStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withProps(() => ({
    _laborService: inject(LaborService),
    _messageService: inject(MessageService),
    _confirmationService: inject(ConfirmationService),
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.labors().length === 0)
  })),

  withMethods((store) => {

    const showMessage = (message: string, severity: 'success' | 'error') => {
      store._messageService.add({
        severity,
        summary: severity === 'success' ? 'Éxito' : 'Error',
        detail: message,
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

      messages.forEach((msg) => showMessage(msg, 'error'));

      const errorForState = messages.join('\n');
      patchState(store, { loading: false, error: errorForState });
    };

    const loadLabors = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: LaborPageableRequest = {
            name: filters.name,
            isPiecework: filters.isPiecework,
            laborUnitPublicId: filters.laborUnitPublicId,
            page: filters.page,
            size: filters.size,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._laborService.getAll(params).pipe(
            tap((response: ApiResult<PagedResult<LaborListDTO>>) => {
              if (response.success) {
                patchState(store, {
                  labors: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar labores', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar labores');
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._laborService.getSelectOptions().pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { selectOptions: response.data });
              } else {
                handleHttpError(null, 'Error al cargar opciones de labores', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar opciones de labores');
              return of(null);
            })
          )
        )
      )
    );

    const deleteLabor = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((publicId) =>
          store._laborService.delete(publicId).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { loading: false });
                showMessage(response.message, 'success');
                loadLabors();
                loadSelectOptions();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al eliminar labor', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al eliminar labor');
              return of(null);
            })
          )
        )
      )
    );

    return {
      setFilters(filters: Partial<LaborFilters>) {
        patchState(store, { filters: { ...store.filters(), ...filters } });
        loadLabors();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadLabors();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), name: '', page: 0 }
        });
        loadLabors();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            name: '',
            isPiecework: undefined,
            laborUnitPublicId: undefined,
            page: 0,
            size: 10,
            sortBy: 'name',
            sortDirection: 'asc'
          }
        });
        loadLabors();
      },

      loadPage: (params: { page: number; size: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadLabors();
      },

      create: rxMethod<CreateLaborRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._laborService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadLabors();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear labor', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear labor');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateLaborRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request }) =>
            store._laborService.update(publicId, request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadLabors();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar labor', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error de conexión al actualizar labor');
                return of(null);
              })
            )
          )
        )
      ),

      delete: deleteLabor,

      confirmDelete: (labor: LaborListDTO) => {
        store._confirmationService.confirm({
          message: `¿Estás seguro de que quieres eliminar la labor ${labor.name}?`,
          header: 'Confirmar Eliminación',
          icon: 'pi pi-exclamation-triangle',
          accept: () => {
            deleteLabor(labor.publicId);
          }
        });
      },

      refresh: () => {
        loadLabors();
        loadSelectOptions();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      init: () => {
        loadSelectOptions();
        loadLabors();
      }
    };
  })
);