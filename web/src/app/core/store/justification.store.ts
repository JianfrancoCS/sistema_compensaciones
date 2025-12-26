import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { ApiResult, PagedResult } from '@shared/types/api';
import { MessageService } from 'primeng/api';
import { JustificationService } from '../services/justification.service';
import {
  JustificationListDTO,
  JustificationDetailsDTO,
  CreateJustificationRequest,
  UpdateJustificationRequest,
  JustificationPageableRequest,
  JustificationSelectOptionDTO,
  JustificationState
} from '@shared/types/justification';

export type { JustificationListDTO, JustificationDetailsDTO, CreateJustificationRequest, UpdateJustificationRequest };

const initialState: JustificationState = {
  justifications: [],
  loading: false,
  error: null,
  totalElements: 0,
  selectOptions: [],
  filters: {
    name: '',
    isPaid: undefined,
    page: 0,
    size: 10, // Changed from pageSize to size
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  }
};

export const JustificationStore = signalStore(
  withState<JustificationState>(initialState),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.justifications().length === 0)
  })),

  withMethods((store) => {
    const _justificationService = inject(JustificationService);
    const _messageService = inject(MessageService);

    const showMessage = (message: string, severity: 'success' | 'error') => {
      _messageService.add({
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

    const loadJustifications = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: JustificationPageableRequest = {
            name: filters.name,
            isPaid: filters.isPaid,
            page: filters.page,
            size: filters.size, // Changed from pageSize to size
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return _justificationService.getJustifications(params).pipe(
            tap((response: ApiResult<PagedResult<JustificationListDTO>>) => {
              if (response.success) {
                patchState(store, {
                  justifications: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error de conexión al cargar justificaciones', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar justificaciones');
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          _justificationService.getSelectOptions().pipe(
            tap((response: ApiResult<JustificationSelectOptionDTO[]>) => {
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
        loadJustifications();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadJustifications();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), name: '', page: 0 }
        });
        loadJustifications();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            name: '',
            isPaid: undefined,
            page: 0,
            size: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC'
          }
        });
        loadJustifications();
      },

      loadPage: (params: { page: number; size: number; sortBy: string; sortDirection: string }) => { // Changed pageSize to size
        patchState(store, {
          filters: { ...store.filters(), page: params.page, size: params.size, sortBy: params.sortBy, sortDirection: params.sortDirection }
        });
        loadJustifications();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadJustifications();
      },

      create: rxMethod<CreateJustificationRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            _justificationService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadJustifications();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear la justificación', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear la justificación');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateJustificationRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request }) =>
            _justificationService.update(publicId, request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadJustifications();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar la justificación', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar la justificación');
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
            _justificationService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadJustifications();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar la justificación', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar la justificación');
                return of(null);
              })
            )
          )
        )
      ),

      refresh: () => {
        loadJustifications();
        loadSelectOptions();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      getDetails: (publicId: string) => {
        return _justificationService.getDetails(publicId);
      },

      init: () => {
        loadSelectOptions();
        loadJustifications();
      }
    };
  })
);
