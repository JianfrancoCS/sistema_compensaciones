import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { DynamicVariableService } from '../services/dynamic-variable.service';
import {
  DynamicVariableListDTO,
  CreateDynamicVariableRequest,
  UpdateDynamicVariableRequest,
  CommandDynamicVariableResponse
} from '@shared/types/dynamic-variable';

export interface ValidationRule {
  code: string;
  name: string;
  regex: string;
  errorMessage: string;
  isActive: boolean;
}

export interface DynamicVariableFilters {
  name: string;
  isActive?: boolean;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export interface DynamicVariableState {
  variables: DynamicVariableListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  validationRules: Map<string, ValidationRule>;
  filters: DynamicVariableFilters;
}

const initialState: DynamicVariableState = {
  variables: [],
  loading: false,
  error: null,
  totalElements: 0,
  validationRules: new Map(),
  filters: {
    name: '',
    isActive: true,
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  }
};

export const DynamicVariableStore = signalStore(
  withState<DynamicVariableState>(initialState),

  withProps(() => ({
    _dynamicVariableService: inject(DynamicVariableService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.variables().length === 0),
    validationRulesArray: computed(() => Array.from(state.validationRules().values())),
    activeValidationRules: computed(() =>
      Array.from(state.validationRules().values()).filter(rule => rule.isActive)
    )
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
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params = {
            name: filters.name,
            isActive: filters.isActive,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._dynamicVariableService.getDynamicVariables(params).pipe(
            tap((response) => {
              if (response.success) {
                const validationRules = new Map<string, ValidationRule>();

                response.data.data.forEach(variable => {
                  if (variable.isActive && variable.finalRegex) {
                    validationRules.set(variable.code, {
                      code: variable.code,
                      name: variable.name,
                      regex: variable.finalRegex,
                      errorMessage: variable.errorMessage,
                      isActive: variable.isActive
                    });
                  }
                });

                patchState(store, {
                  variables: response.data.data,
                  totalElements: response.data.totalElements,
                  validationRules,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false, error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error de conexión al cargar variables dinámicas');
              patchState(store, { loading: false, error });
              return of(null);
            })
          );
        })
      )
    );

    return {
      updateFilter: (filters: { name?: string; isActive?: boolean }) => {
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
            isActive: true,
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

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadVariables();
      },

      create: (request: CreateDynamicVariableRequest) => {
        store._dynamicVariableService.create(request).pipe(
          tap((response) => {
            if (response.success) {
              loadVariables();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { error: getErrorMessage(err, 'Error al crear la variable dinámica') });
            return of(null);
          })
        ).subscribe();
      },

      update: (params: { publicId: string; request: UpdateDynamicVariableRequest }) => {
        const { publicId, request } = params;
        store._dynamicVariableService.update(publicId, request).pipe(
          tap((response) => {
            if (response.success) {
              loadVariables();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { error: getErrorMessage(err, 'Error al actualizar la variable dinámica') });
            return of(null);
          })
        ).subscribe();
      },

      delete: (publicId: string) => {
        store._dynamicVariableService.delete(publicId).pipe(
          tap((response) => {
            if (response.success) {
              loadVariables();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            patchState(store, { error: getErrorMessage(err, 'Error al eliminar la variable dinámica') });
            return of(null);
          })
        ).subscribe();
      },

      getValidationRule: (code: string): ValidationRule | undefined => {
        return store.validationRules().get(code);
      },

      validateValue: (value: string, variableCode: string): { isValid: boolean; errorMessage?: string } => {
        const rule = store.validationRules().get(variableCode);

        if (!rule || !rule.isActive || !rule.regex) {
          return { isValid: true };
        }

        if (!value) {
          return { isValid: true };
        }

        try {
          const regex = new RegExp(rule.regex);
          const isValid = regex.test(value);

          return {
            isValid,
            errorMessage: isValid ? undefined : (rule.errorMessage || `El formato no es válido para ${rule.name}`)
          };
        } catch (error) {
          console.warn(`Invalid regex pattern for variable ${variableCode}:`, rule.regex);
          return { isValid: true };
        }
      },

      hasValidationRule: (code: string): boolean => {
        return store.validationRules().has(code);
      },

      loadValidationRules: () => {
        const originalFilters = store.filters();
        patchState(store, {
          filters: { ...originalFilters, isActive: true, pageSize: 100 }
        });
        loadVariables();
        patchState(store, { filters: originalFilters });
      },

      refresh: () => {
        loadVariables();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      init: () => {
        loadVariables();
      }
    };
  })
);