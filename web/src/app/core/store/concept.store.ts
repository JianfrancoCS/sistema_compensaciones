import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of, exhaustMap, finalize } from 'rxjs';
import { ApiResult, PagedResult } from '@shared/types/api';
import { ConceptService } from '../services/concept.service';
import { MessageService } from 'primeng/api';
import {
  ConceptListDTO,
  ConceptDetailsDTO,
  CreateConceptRequest,
  UpdateConceptRequest,
  ConceptParams,
  ConceptState,
  ConceptSelectOptionDTO
} from '@shared/types/concept';

export type { ConceptListDTO, ConceptDetailsDTO, CreateConceptRequest, UpdateConceptRequest };

const initialState: ConceptState = {
  concepts: [],
  loading: false,
  error: null,
  totalElements: 0,
  selectOptions: [],
  filters: {
    name: '',
    categoryPublicId: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  },
  categories: [],
  loadingCategories: false,
  conceptsByCategory: {} as Record<string, ConceptSelectOptionDTO[]>,
  loadingConceptsByCategory: {} as Record<string, boolean>
};

export const ConceptStore = signalStore(
  { providedIn: 'root' },
  withState<ConceptState>(initialState),

  withProps(() => ({
    _conceptService: inject(ConceptService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.concepts().length === 0)
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

    const loadConcepts = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: ConceptParams = {
            name: filters.name,
            categoryPublicId: filters.categoryPublicId || undefined,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._conceptService.getConcepts(params).pipe(
            tap((response: ApiResult<PagedResult<ConceptListDTO>>) => {
              if (response.success) {
                patchState(store, {
                  concepts: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error de conexión al cargar conceptos', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar conceptos');
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._conceptService.getSelectOptions().pipe(
            tap((response: ApiResult<ConceptSelectOptionDTO[]>) => {
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
        loadConcepts();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadConcepts();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), name: '', page: 0 }
        });
        loadConcepts();
      },

      updateCategoryFilter: (categoryPublicId: string) => {
        patchState(store, {
          filters: { ...store.filters(), categoryPublicId, page: 0 }
        });
        loadConcepts();
      },

      clearCategoryFilter: () => {
        patchState(store, {
          filters: { ...store.filters(), categoryPublicId: '', page: 0 }
        });
        loadConcepts();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            name: '',
            categoryPublicId: '',
            page: 0,
            pageSize: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC'
          }
        });
        loadConcepts();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadConcepts();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadConcepts();
      },

      create: rxMethod<CreateConceptRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._conceptService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadConcepts();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el concepto', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear el concepto');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateConceptRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request }) =>
            store._conceptService.update(publicId, request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadConcepts();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar el concepto', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar el concepto');
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
            store._conceptService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadConcepts();
                  loadSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar el concepto', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar el concepto');
                return of(null);
              })
            )
          )
        )
      ),

      refresh: () => {
        loadConcepts();
        loadSelectOptions();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      getDetails: (publicId: string) => {
        return store._conceptService.getDetails(publicId);
      },

      loadCategories: rxMethod<void>(
        pipe(
          tap(() => patchState(store, { loadingCategories: true, error: null })),
          switchMap(() =>
            store._conceptService.getCategories().pipe(
              tap((response) => {
                if (response.success && response.data) {
                  patchState(store, {
                    categories: response.data,
                    loadingCategories: false
                  });
                } else {
                  patchState(store, { loadingCategories: false });
                  handleHttpError(null, 'Error al cargar categorías', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al cargar categorías');
                patchState(store, { loadingCategories: false });
                return of(null);
              })
            )
          )
        )
      ),

      init: () => {
        loadSelectOptions();
        loadConcepts();
      },

      loadSelectOptionsByCategory: rxMethod<string>(
        pipe(
          switchMap((categoryCode) => {
            const cached = store.conceptsByCategory()[categoryCode];
            if (cached && cached.length > 0) {
              const currentLoading = { ...store.loadingConceptsByCategory() };
              if (currentLoading[categoryCode]) {
                currentLoading[categoryCode] = false;
                patchState(store, { loadingConceptsByCategory: currentLoading });
              }
              return of(null);
            }

            const currentLoading = { ...store.loadingConceptsByCategory() };
            currentLoading[categoryCode] = true;
            patchState(store, { loadingConceptsByCategory: currentLoading });

            return store._conceptService.getSelectOptionsByCategory(categoryCode).pipe(
              tap((response: ApiResult<ConceptSelectOptionDTO[]>) => {
                if (response.success && response.data) {
                  const currentState = store.conceptsByCategory();
                  
                  const updatedCategories: Record<string, ConceptSelectOptionDTO[]> = {};
                  
                  for (const key in currentState) {
                    if (currentState.hasOwnProperty(key)) {
                      updatedCategories[key] = [...(currentState[key] || [])];
                    }
                  }
                  
                  updatedCategories[categoryCode] = [...response.data];
                  
                  patchState(store, { conceptsByCategory: updatedCategories });
                } else {
                  handleHttpError(null, `Error al cargar conceptos de categoría ${categoryCode}`, response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, `Error al cargar conceptos de categoría ${categoryCode}`);
                return of(null);
              }),
              finalize(() => {
                const finalLoading = { ...store.loadingConceptsByCategory() };
                finalLoading[categoryCode] = false;
                patchState(store, { loadingConceptsByCategory: finalLoading });
              })
            );
          })
        )
      ),

      getSelectOptionsByCategory: (categoryCode: string): ConceptSelectOptionDTO[] => {
        return store.conceptsByCategory()[categoryCode] || [];
      },

      isLoadingCategory: (categoryCode: string): boolean => {
        return store.loadingConceptsByCategory()[categoryCode] || false;
      }
    };
  })
);

