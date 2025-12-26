import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of, exhaustMap } from 'rxjs';
import { SelectOption, ApiResult, PagedResult } from '@shared/types/api';
import { AreaService } from '../services/area.service';
import { MessageService } from 'primeng/api';
import {
  AreaListDTO,
  AreaDetailsDTO,
  CreateAreaRequest,
  UpdateAreaRequest,
  AreaParams,
  AreaState,
  AreaSelectOptionDTO
} from '@shared/types/area';

export type { AreaListDTO, AreaDetailsDTO, CreateAreaRequest, UpdateAreaRequest };

const initialState: AreaState = {
  areas: [],
  loading: false,
  error: null,
  totalElements: 0,
  selectOptions: [],
  filters: {
    name: '',
    subsidiaryPublicId: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  }
};

export const AreaStore = signalStore(
  withState<AreaState>(initialState),

  withProps(() => ({
    _areaService: inject(AreaService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.areas().length === 0)
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

    const loadAreas = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: AreaParams = {
            name: filters.name,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._areaService.getAreas(params).pipe(
            tap((response: ApiResult<PagedResult<AreaListDTO>>) => {
              if (response.success) {
                patchState(store, {
                  areas: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error de conexión al cargar áreas', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar áreas');
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._areaService.getSelectOptions().pipe(
            tap((response: ApiResult<AreaSelectOptionDTO[]>) => {
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
        loadAreas();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadAreas();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), name: '', page: 0 }
        });
        loadAreas();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            name: '',
            subsidiaryPublicId: '',
            page: 0,
            pageSize: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC'
          }
        });
        loadAreas();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadAreas();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadAreas();
      },

      create: rxMethod<CreateAreaRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._areaService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadAreas();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el área', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear el área');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateAreaRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request }) =>
            store._areaService.update(publicId, request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadAreas();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar el área', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar el área');
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
            store._areaService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadAreas();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar el área', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar el área');
                return of(null);
              })
            )
          )
        )
      ),

      refresh: () => {
        loadAreas();
        loadSelectOptions();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      getDetails: (publicId: string) => {
        return store._areaService.getDetails(publicId);
      },

      init: () => {
        loadSelectOptions();
        loadAreas();
      }
    };
  })
);
