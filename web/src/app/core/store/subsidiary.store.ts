import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { SelectOption } from '@shared/types/api';
import { SubsidiaryListDTO, SubsidiaryDetailsDTO, CreateSubsidiaryRequest, UpdateSubsidiaryRequest, SubsidiaryParams } from '@shared/types/subsidiary';
import { SubsidiaryService } from '../services/subsidiary.service';
import { MessageService } from 'primeng/api';

export type {
  SubsidiaryListDTO,
  SubsidiaryDetailsDTO,
  CreateSubsidiaryRequest,
  UpdateSubsidiaryRequest
};

interface SubsidiaryFilters {
  name: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface SubsidiaryState {
  subsidiaries: SubsidiaryListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: SelectOption[];
  filters: SubsidiaryFilters;
}

const initialState: SubsidiaryState = {
  subsidiaries: [],
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

export const SubsidiaryStore = signalStore(
  withState<SubsidiaryState>(initialState),

  withProps(() => ({
    _subsidiaryService: inject(SubsidiaryService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.subsidiaries().length === 0)
  })),

  withMethods((store) => {
    const showMessage = (message: string, severity: 'success' | 'error') => {
      store._messageService.add({
        severity,
        summary: severity === 'success' ? 'Éxito' : 'Error',
        detail: message
      });
    };

    const processHttpError = (err: any, defaultMessage: string, apiResponseMessage?: string): void => {
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

    const loadSubsidiaries = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: SubsidiaryParams = {
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          if (filters.name) {
            params.name = filters.name;
          }

          return store._subsidiaryService.getSubsidiaries(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  subsidiaries: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                processHttpError(null, 'Error al cargar filiales', response.message);
              }
            }),
            catchError((err) => {
              processHttpError(err, 'Error de conexión al cargar filiales');
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._subsidiaryService.getSelectOptions().pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { selectOptions: response.data });
              } else {
                patchState(store, { error: response.message });
                processHttpError(null, 'Error al cargar opciones de selección', response.message);
              }
            }),
            catchError((err) => {
              processHttpError(err, 'Error al cargar opciones de selección');
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
        loadSubsidiaries();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadSubsidiaries();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), name: '', page: 0 }
        });
        loadSubsidiaries();
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
        loadSubsidiaries();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadSubsidiaries();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadSubsidiaries();
      },

      create: rxMethod<CreateSubsidiaryRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._subsidiaryService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadSubsidiaries();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  processHttpError(null, 'Error al crear la filial', response.message);
                }
              }),
              catchError((err) => {
                processHttpError(err, 'Error al crear la filial');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateSubsidiaryRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request }) =>
            store._subsidiaryService.update(publicId, request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadSubsidiaries();
                } else {
                  patchState(store, { loading: false });
                  processHttpError(null, 'Error al actualizar la filial', response.message);
                }
              }),
              catchError((err) => {
                processHttpError(err, 'Error al actualizar la filial');
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
            store._subsidiaryService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadSubsidiaries();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  processHttpError(null, 'Error al eliminar la filial', response.message);
                }
              }),
              catchError((err) => {
                processHttpError(err, 'Error al eliminar la filial');
                return of(null);
              })
            )
          )
        )
      ),

      refresh: () => {
        loadSubsidiaries();
        loadSelectOptions();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      getDetails: (publicId: string) => {
        return store._subsidiaryService.getDetails(publicId);
      },

      init: () => {
        loadSelectOptions();
        loadSubsidiaries();
      }
    };
  })
);
