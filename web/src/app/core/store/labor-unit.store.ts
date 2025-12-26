import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of,  } from 'rxjs';
import { SelectOption, ApiResult, PagedResult } from '@shared/types/api';
import { LaborUnitService } from '../services/labor-unit.service';
import { MessageService } from 'primeng/api';
import {
  LaborUnitListDTO,
  LaborUnitDetailsDTO,
  CreateLaborUnitRequest,
  UpdateLaborUnitRequest,
  LaborUnitParams,
  LaborUnitState,
  LaborUnitSelectOptionDTO
} from '@shared/types/labor-unit';

export type { LaborUnitListDTO, LaborUnitDetailsDTO, CreateLaborUnitRequest, UpdateLaborUnitRequest };

const initialState: LaborUnitState = {
  laborUnits: [],
  loading: false,
  error: null,
  totalElements: 0,
  selectOptions: [],
  filters: {
    name: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  }
};

export const LaborUnitStore = signalStore(
  withState<LaborUnitState>(initialState),

  withProps(() => ({
    _laborUnitService: inject(LaborUnitService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.laborUnits().length === 0)
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

    const loadLaborUnits = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: LaborUnitParams = {
            name: filters.name,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._laborUnitService.getLaborUnits(params).pipe(
            tap((response: ApiResult<PagedResult<LaborUnitListDTO>>) => {
              if (response.success) {
                patchState(store, {
                  laborUnits: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error de conexión al cargar unidades de labor', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar unidades de labor');
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptionsMethod = rxMethod<void>( // Renamed to avoid conflict with returned method name
      pipe(
        switchMap(() =>
          store._laborUnitService.getSelectOptions().pipe(
            tap((response: ApiResult<LaborUnitSelectOptionDTO[]>) => {
              if (response.success) {
                patchState(store, { selectOptions: response.data });
              } else {
                patchState(store, { error: response.message });
                handleHttpError(null, 'Error al cargar opciones de selección', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar opciones de selección');
              return of(null);
            })
          )
        )
      )
    );

    return {
      updateFilter: (name: string) => {
        patchState(store, {
          filters: { ...store.filters(), name, page: 0 }
        });
        loadLaborUnits();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadLaborUnits();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), name: '', page: 0 }
        });
        loadLaborUnits();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            name: '',
            page: 0,
            pageSize: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC'
          }
        });
        loadLaborUnits();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadLaborUnits();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadLaborUnits();
      },

      create: rxMethod<CreateLaborUnitRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._laborUnitService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadLaborUnits();
                  loadSelectOptionsMethod(); // Call the internal method
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear la unidad de labor', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear la unidad de labor');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateLaborUnitRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request }) =>
            store._laborUnitService.update(publicId, request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadLaborUnits();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar la unidad de labor', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar la unidad de labor');
                return of(null);
              })
            )
          )
        )
      ),

      delete: rxMethod<string>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((publicId) =>
            store._laborUnitService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadLaborUnits();
                  loadSelectOptionsMethod(); // Call the internal method
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar la unidad de labor', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar la unidad de labor');
                return of(null);
              })
            )
          )
        )
      ),

      refresh: () => {
        loadLaborUnits();
        loadSelectOptionsMethod(); // Call the internal method
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      getDetails: (publicId: string) => {
        return store._laborUnitService.getDetails(publicId);
      },

      init: () => {
        loadSelectOptionsMethod(); // Call the internal method
        loadLaborUnits();
      },

      loadSelectOptions: loadSelectOptionsMethod // Expose the method publicly
    };
  })
);
