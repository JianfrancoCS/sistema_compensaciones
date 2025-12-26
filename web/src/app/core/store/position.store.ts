import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { SelectOption } from '@shared/types/api';
import {
  Position as PositionListDTO,
  CreatePositionRequest,
  UpdatePositionRequest,
  PositionParams
} from '@shared/types/position';
import { PositionService } from '../services/position.service';
import { MessageService } from 'primeng/api';
import { AreaStore } from './area.store';

interface PositionFilters {
  name: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface PositionState {
  positions: PositionListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  positionSelectOptions: SelectOption[];
  positionSelectOptionsAll: SelectOption[];
  filters: PositionFilters;
}

const initialState: PositionState = {
  positions: [],
  loading: false,
  error: null,
  totalElements: 0,
  positionSelectOptions: [],
  positionSelectOptionsAll: [],
  filters: {
    name: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  }
};

export const PositionStore = signalStore(
  { providedIn: 'root' },
  withState<PositionState>(initialState),

  withProps(() => ({
    _positionService: inject(PositionService),
    _messageService: inject(MessageService),
    _areaStore: inject(AreaStore)
  })),

  withComputed((store) => ({
    isEmpty: computed(() => !store.loading() && store.positions().length === 0),
    areaSelectOptions: store._areaStore.selectOptions
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

    const loadPositions = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: PositionParams = {
            name: filters.name,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._positionService.getPositions(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  positions: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar cargos', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar cargos');
              return of(null);
            })
          );
        })
      )
    );

    const loadPositionSelectOptionsRx = rxMethod<string | undefined>(
      pipe(
        switchMap((areaPublicId) =>
          store._positionService.getPositionsSelectOptions(areaPublicId).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { positionSelectOptions: response.data });
              } else {
                patchState(store, { error: response.message });
                handleHttpError(null, 'Error al cargar opciones de selección de cargos', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar opciones de selección de cargos');
              return of(null);
            })
          )
        )
      )
    );

    const loadAllPositionSelectOptionsRx = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._positionService.getPositionsSelectOptions().pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { positionSelectOptionsAll: response.data });
              } else {
                patchState(store, { error: response.message });
                handleHttpError(null, 'Error al cargar todas las opciones de selección de cargos', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar todas las opciones de selección de cargos');
              return of(null);
            })
          )
        )
      )
    );

    return {
      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadPositions();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), name: '', page: 0 }
        });
        loadPositions();
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
        loadPositions();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadPositions();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadPositions();
      },

      create: rxMethod<CreatePositionRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._positionService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadPositions();
                  loadPositionSelectOptionsRx(undefined);
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el cargo', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear el cargo');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdatePositionRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((params) =>
            store._positionService.update(params.publicId, params.request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadPositions();
                  loadPositionSelectOptionsRx(undefined);
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar el cargo', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar el cargo');
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
            store._positionService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadPositions();
                  loadPositionSelectOptionsRx(undefined);
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar el cargo', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar el cargo');
                return of(null);
              })
            )
          )
        )
      ),

      refresh: () => {
        loadPositions();
        store._areaStore.refresh();
        loadPositionSelectOptionsRx(undefined);
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      getDetails: (publicId: string) => {
        return store._positionService.getPositionDetailsForUpdate(publicId);
      },

      loadPositionSelectOptionsByArea: (areaPublicId: string | undefined) => {
        loadPositionSelectOptionsRx(areaPublicId);
      },

      init: () => {
        store._areaStore.init();
        if (store.positionSelectOptions().length === 0) {
          loadPositionSelectOptionsRx(undefined);
        }
        if (store.positionSelectOptionsAll().length === 0) {
          loadAllPositionSelectOptionsRx();
        }
        if (store.positions().length === 0) {
          loadPositions();
        }
      },
    };
  })
);
