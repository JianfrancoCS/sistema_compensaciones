import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { MessageService } from 'primeng/api';
import { VariableService } from '../services/variable.service';
import {
  VariableListDTO,
  CreateVariableRequest,
  UpdateVariableRequest,
  CreateVariableWithValidationRequest,
  UpdateVariableWithValidationRequest,
  VariableParams,
  VariableFilters,
  VariableState,
  VariableSelectOption,
  ValidationMethodDTO,
  VariableValidationRequest,
  VariableValidationDTO,
  VariableSelectOptionsParams
} from '@shared/types/variable';
export type { VariableListDTO, CreateVariableRequest, UpdateVariableRequest };

const initialState: VariableState = {
  variables: [],
  loading: false,
  error: null,
  totalElements: 0,
  selectOptions: [],
  filters: {
    name: '',
    code: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  },
  validationMethods: [],
  currentVariableValidation: null,
  validationLoading: false,
  validationError: null,
  searchLoading: false
};

const showToast = (messageService: MessageService, response: any, defaultErrorMessage?: string) => {
  if (response.success) {
    messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: response.message
    });
  } else {
    const errorMsg = response.message || defaultErrorMessage || 'Error en la operación';
    messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: errorMsg
    });
  }
};

export const VariableStore = signalStore(
  withState<VariableState>(initialState),

  withProps(() => ({
    _variableService: inject(VariableService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.variables().length === 0)
  })),

  withMethods((store) => {
    const getErrorMessage = (err: any, defaultMessage: string): string => {
      if (err?.error && typeof err.error === 'object' && 'message' in err.error) {
        return err.error.message;
      }
      return err?.message || defaultMessage;
    };

    const loadVariables = rxMethod<void>(
      pipe(
        tap(() => {
          patchState(store, { loading: true, error: null });
        }),
        switchMap(() => {
          const filters = store.filters();
          const params: VariableParams = {
            name: filters.name,
            code: filters.code,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._variableService.getVariables(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  variables: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false, error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error de conexión al cargar variables');
              patchState(store, { loading: false, error });
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptions = rxMethod<VariableSelectOptionsParams | void>(
      pipe(
        tap(() => patchState(store, { searchLoading: true })),
        switchMap((params) =>
          store._variableService.getSelectOptions(params || undefined).pipe(
            tap((response) => {
              patchState(store, {
                selectOptions: response.success ? response.data : [],
                searchLoading: false
              });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar opciones de selección');
              patchState(store, { error, searchLoading: false });
              return of(null);
            })
          )
        )
      )
    );

    const loadValidationMethods = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { validationLoading: true, validationError: null })),
        switchMap(() =>
          store._variableService.getValidationMethods().pipe(
            tap((response) => {
              patchState(store, {
                validationMethods: response.success ? response.data : [],
                validationLoading: false
              });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar métodos de validación');
              patchState(store, { validationError: error, validationLoading: false });
              return of(null);
            })
          )
        )
      )
    );

    const loadVariableValidation = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { validationLoading: true, validationError: null })),
        switchMap((variableId) =>
          store._variableService.getVariableWithValidation(variableId).pipe(
            tap((response) => {
              patchState(store, {
                currentVariableValidation: response.success ? response.data : null,
                validationLoading: false
              });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar validación de variable');
              patchState(store, { validationError: error, validationLoading: false });
              return of(null);
            })
          )
        )
      )
    );

    return {
      updateFilter: (filters: { name?: string; code?: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...filters, page: 0 }
        });
        loadVariables();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadVariables();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            name: '',
            code: '',
            page: 0,
            pageSize: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC'
          }
        });
        loadVariables();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadVariables();
      },
      onLazyLoad: (event: any) => {
        const page = (event.first ?? 0) / (event.rows ?? 10);
        const pageSize = event.rows ?? 10;

        let sortBy = 'createdAt';
        if (event.sortField) {
          if (Array.isArray(event.sortField)) {
            sortBy = event.sortField[0] || 'createdAt';
          } else {
            sortBy = event.sortField;
          }
        }

        const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

        patchState(store, {
          filters: { ...store.filters(), page, pageSize, sortBy, sortDirection }
        });
        loadVariables();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadVariables();
      },

      create: (request: CreateVariableRequest) => {
        patchState(store, { loading: true, error: null });

        store._variableService.create(request).pipe(
          tap((response) => {
            patchState(store, { loading: false });
            showToast(store._messageService, response, 'Error al crear la variable');
            if (response.success) {
              loadVariables();
              loadSelectOptions();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { loading: false });
            if (err.error && err.error.message) {
              const apiError = err.error;
              showToast(store._messageService, apiError, 'Error al crear la variable');
              patchState(store, { error: apiError.message });
            } else {
              const errorMsg = getErrorMessage(err, 'Error de conexión al crear la variable');
              patchState(store, { error: errorMsg });
              store._messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: errorMsg
              });
            }
            return of(null);
          })
        ).subscribe();
      },

      createWithValidation: (request: CreateVariableWithValidationRequest) => {
        patchState(store, { loading: true, error: null });

        store._variableService.createWithValidation(request).pipe(
          tap((response) => {
            patchState(store, { loading: false });
            showToast(store._messageService, response, 'Error al crear la variable con validación');
            if (response.success) {
              loadVariables();
              loadSelectOptions();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { loading: false });
            if (err.error && err.error.message) {
              const apiError = err.error;
              showToast(store._messageService, apiError, 'Error al crear la variable con validación');
              patchState(store, { error: apiError.message });
            } else {
              const errorMsg = getErrorMessage(err, 'Error de conexión al crear la variable con validación');
              patchState(store, { error: errorMsg });
              store._messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: errorMsg
              });
            }
            return of(null);
          })
        ).subscribe();
      },

      update: (params: { publicId: string; request: UpdateVariableRequest }) => {
        const { publicId, request } = params;
        patchState(store, { loading: true, error: null });

        store._variableService.update(publicId, request).pipe(
          tap((response) => {
            patchState(store, { loading: false });
            showToast(store._messageService, response, 'Error al actualizar la variable');
            if (response.success) {
              loadVariables();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { loading: false });
            if (err.error && err.error.message) {
              const apiError = err.error;
              showToast(store._messageService, apiError, 'Error al actualizar la variable');
              patchState(store, { error: apiError.message });
            } else {
              const errorMsg = getErrorMessage(err, 'Error de conexión al actualizar la variable');
              patchState(store, { error: errorMsg });
              store._messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: errorMsg
              });
            }
            return of(null);
          })
        ).subscribe();
      },

      updateWithValidation: (params: { publicId: string; request: UpdateVariableWithValidationRequest }) => {
        const { publicId, request } = params;
        patchState(store, { loading: true, error: null });

        store._variableService.updateWithValidation(publicId, request).pipe(
          tap((response) => {
            patchState(store, { loading: false });
            showToast(store._messageService, response, 'Error al actualizar la variable con validación');
            if (response.success) {
              loadVariables();
              loadSelectOptions();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { loading: false });
            if (err.error && err.error.message) {
              const apiError = err.error;
              showToast(store._messageService, apiError, 'Error al actualizar la variable con validación');
              patchState(store, { error: apiError.message });
            } else {
              const errorMsg = getErrorMessage(err, 'Error de conexión al actualizar la variable con validación');
              patchState(store, { error: errorMsg });
              store._messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: errorMsg
              });
            }
            return of(null);
          })
        ).subscribe();
      },

      delete: (publicId: string) => {
        patchState(store, { loading: true, error: null });

        store._variableService.delete(publicId).pipe(
          tap((response) => {
            patchState(store, { loading: false });
            showToast(store._messageService, response, 'Error al eliminar la variable');
            if (response.success) {
              loadVariables();
              loadSelectOptions();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { loading: false });
            if (err.error && err.error.message) {
              const apiError = err.error;
              showToast(store._messageService, apiError, 'Error al eliminar la variable');
              patchState(store, { error: apiError.message });
            } else {
              const errorMsg = getErrorMessage(err, 'Error de conexión al eliminar la variable');
              patchState(store, { error: errorMsg });
              store._messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: errorMsg
              });
            }
            return of(null);
          })
        ).subscribe();
      },

      refresh: () => {
        loadVariables();
        loadSelectOptions();
      },

      refreshWithoutPagination: () => {
        patchState(store, {
          filters: { ...store.filters(), page: 0 }
        });
        loadVariables();
        loadSelectOptions();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      searchSelectOptions: (params?: VariableSelectOptionsParams) => {
        if (store.selectOptions().length === 0 || params?.name) {
          loadSelectOptions(params);
        }
      },

      loadValidationMethods: () => {
        if (store.validationMethods().length === 0) {
          loadValidationMethods();
        }
      },

      loadVariableValidation: (variableId: string) => {
        loadVariableValidation(variableId);
      },

      associateValidationMethods: (params: { variableId: string; request: VariableValidationRequest }) => {
        const { variableId, request } = params;
        patchState(store, { validationLoading: true, validationError: null });

        store._variableService.associateValidationMethods(variableId, request).pipe(
          tap((response) => {
            patchState(store, { validationLoading: false });
            showToast(store._messageService, response, 'Error al asociar métodos de validación');
            if (response.success) {
              loadVariableValidation(variableId);
              loadVariables();
            } else {
              patchState(store, { validationError: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { validationLoading: false });
            if (err.error && err.error.message) {
              const apiError = err.error;
              showToast(store._messageService, apiError, 'Error al asociar métodos de validación');
              patchState(store, { validationError: apiError.message });
            } else {
              const error = getErrorMessage(err, 'Error de conexión al asociar métodos de validación');
              patchState(store, { validationError: error });
              store._messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: error
              });
            }
            return of(null);
          })
        ).subscribe();
      },

      updateValidationMethods: (params: { variableId: string; request: VariableValidationRequest }) => {
        const { variableId, request } = params;
        patchState(store, { validationLoading: true, validationError: null });

        store._variableService.updateValidationMethods(variableId, request).pipe(
          tap((response) => {
            patchState(store, { validationLoading: false });
            showToast(store._messageService, response, 'Error al actualizar métodos de validación');
            if (response.success) {
              loadVariableValidation(variableId);
              loadVariables();
            } else {
              patchState(store, { validationError: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { validationLoading: false });
            if (err.error && err.error.message) {
              const apiError = err.error;
              showToast(store._messageService, apiError, 'Error al actualizar métodos de validación');
              patchState(store, { validationError: apiError.message });
            } else {
              const error = getErrorMessage(err, 'Error de conexión al actualizar métodos de validación');
              patchState(store, { validationError: error });
              store._messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: error
              });
            }
            return of(null);
          })
        ).subscribe();
      },

      removeValidationMethods: (variableId: string) => {
        patchState(store, { validationLoading: true, validationError: null });

        store._variableService.removeValidationMethods(variableId).pipe(
          tap((response) => {
            patchState(store, { validationLoading: false });
            showToast(store._messageService, response, 'Error al eliminar métodos de validación');
            if (response.success) {
              patchState(store, { currentVariableValidation: null });
              loadVariables();
            } else {
              patchState(store, { validationError: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { validationLoading: false });
            if (err.error && err.error.message) {
              const apiError = err.error;
              showToast(store._messageService, apiError, 'Error al eliminar métodos de validación');
              patchState(store, { validationError: apiError.message });
            } else {
              const error = getErrorMessage(err, 'Error de conexión al eliminar métodos de validación');
              patchState(store, { validationError: error });
              store._messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: error
              });
            }
            return of(null);
          })
        ).subscribe();
      },

      clearValidationError: () => {
        patchState(store, { validationError: null });
      },

      clearCurrentValidation: () => {
        patchState(store, { currentVariableValidation: null });
      },

      init: () => {
        if (store.selectOptions().length === 0) {
          loadSelectOptions();
        }
        if (store.validationMethods().length === 0) {
          loadValidationMethods();
        }
        loadVariables();
      }
    };
  })
);
