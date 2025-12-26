import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { ContractTemplateService } from '../services/contract-template.service';
import { AddendumService } from '../services/addendum.service'; // Import AddendumService
import { MessageService } from 'primeng/api'; // Import MessageService
import {
  ContractTemplateListDTO,
  CommandContractTemplateResponse,
  CreateContractTemplateRequest,
  UpdateContractTemplateRequest,
  ContractTemplatePageableRequest,
  ContractTemplateSelectOptionDTO
} from '@shared/types/contract-template';
import { StateSelectOptionDTO } from '@shared/types/state';

export type {
  ContractTemplateListDTO,
  CommandContractTemplateResponse,
  CreateContractTemplateRequest,
  UpdateContractTemplateRequest,
  StateSelectOptionDTO
};

interface ContractTemplateFilters {
  name: string;
  contractTypePublicId: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface ContractTemplateState {
  contractTemplates: ContractTemplateListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: ContractTemplateSelectOptionDTO[];
  statesSelectOptions: StateSelectOptionDTO[];
  filters: ContractTemplateFilters;
}

const initialState: ContractTemplateState = {
  contractTemplates: [],
  loading: false,
  error: null,
  totalElements: 0,
  selectOptions: [],
  statesSelectOptions: [],
  filters: {
    name: '',
    contractTypePublicId: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  }
};

export const ContractTemplateStore = signalStore(
  withState<ContractTemplateState>(initialState),

  withProps(() => ({
    _contractTemplateService: inject(ContractTemplateService),
    _addendumService: inject(AddendumService), // Inject AddendumService
    _messageService: inject(MessageService) // Inject MessageService
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.contractTemplates().length === 0)
  })),

  withMethods((store) => {
    const messageService = store._messageService;

    const showMessage = (message: string, severity: 'success' | 'error') => {
      messageService.add({
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

    const loadContractTemplates = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: ContractTemplatePageableRequest = {
            name: filters.name,
            contractTypePublicId: filters.contractTypePublicId,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._contractTemplateService.getContractTemplates(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  contractTemplates: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar plantillas de contrato', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar plantillas de contrato');
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._contractTemplateService.getContractTemplateSelectOptions().pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { selectOptions: response.data });
              } else {
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

    const loadStatesSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._addendumService.getStatesSelectOptions().pipe(
            tap((response) => {
               if (response.success) {
                patchState(store, { statesSelectOptions: response.data });
              } else {
                handleHttpError(null, 'Error al cargar estados', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar estados');
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
        loadContractTemplates();
      },

      filterByContractType: (contractTypePublicId: string) => {
        patchState(store, {
          filters: { ...store.filters(), contractTypePublicId, page: 0 }
        });
        loadContractTemplates();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            name: '',
            contractTypePublicId: '',
            page: 0,
            pageSize: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC'
          }
        });
        loadContractTemplates();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadContractTemplates();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadContractTemplates();
      },

      create: rxMethod<CreateContractTemplateRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._contractTemplateService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message || 'Plantilla de contrato creada exitosamente', 'success');
                  loadContractTemplates();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear la plantilla de contrato', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear la plantilla de contrato');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateContractTemplateRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request }) =>
            store._contractTemplateService.update(publicId, request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message || 'Plantilla de contrato actualizada exitosamente', 'success');
                  loadContractTemplates();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar la plantilla de contrato', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar la plantilla de contrato');
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
            store._contractTemplateService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message || 'Plantilla de contrato eliminada exitosamente', 'success');
                  loadContractTemplates();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar la plantilla de contrato', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar la plantilla de contrato');
                return of(null);
              })
            )
          )
        )
      ),

      refresh: () => {
        loadContractTemplates();
        loadSelectOptions();
        loadStatesSelectOptions(); // Add call here
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      getDetails: (publicId: string) => {
        return store._contractTemplateService.getContractTemplateForCommand(publicId);
      },

      getSelectOptionsByContractType: (contractTypePublicId?: string) => {
        return store._contractTemplateService.getContractTemplateSelectOptions(contractTypePublicId);
      },

      init: () => {
        loadSelectOptions();
        loadStatesSelectOptions(); // Add call here
        loadContractTemplates();
      }
    };
  })
);
