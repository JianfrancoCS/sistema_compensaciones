import { inject, computed, signal } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { MessageService, ConfirmationService } from 'primeng/api';
import {
  BatchListDTO,
  BatchDetailsDTO,
  CreateBatchRequest,
  UpdateBatchRequest,
  BatchPageableRequest,
  BatchState,
  BatchSelectOptionDTO,
  ApiResultPagedBatchListDTO,
  ApiResultCommandBatchResponse,
  ApiResultBatchSelectOptionDTOList,
  ApiResultBatchDetailsDTO,
  ApiResultVoid
} from '@shared/types/batches';
import { BatchService } from '../services/batch.service';
import { SubsidiaryStore } from './subsidiary.store';

const initialState: BatchState = {
  batches: [],
  loading: false,
  error: null,
  totalElements: 0,
  filters: {
    name: '',
    subsidiaryPublicId: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  }
};

export const BatchStore = signalStore(
  withState<BatchState>(initialState),

  withProps(() => ({
    _batchService: inject(BatchService),
    _messageService: inject(MessageService),
    _confirmationService: inject(ConfirmationService),
    _subsidiaryStore: inject(SubsidiaryStore),
    isCreateModalVisible: signal(false),
    isUpdateModalVisible: signal(false),
    selectedBatch: signal<BatchDetailsDTO | null>(null)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.batches().length === 0),
    subsidiarySelectOptions: state._subsidiaryStore.selectOptions // Expose subsidiary select options
  })),

  withMethods((store) => {
    const messageService = store._messageService;
    const confirmationService = store._confirmationService;
    const subsidiaryStore = store._subsidiaryStore;

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

    const loadBatches = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: BatchPageableRequest = {
            name: filters.name,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection,
            subsidiaryPublicId: filters.subsidiaryPublicId
          };

          return store._batchService.getBatches(params).pipe(
            tap((response: ApiResultPagedBatchListDTO) => {
              if (response.success && response.data) {
                patchState(store, {
                  batches: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error de conexión al cargar lotes', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar lotes');
              return of(null);
            })
          );
        })
      )
    );

    const deleteMethod = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((publicId) =>
          store._batchService.deleteBatch(publicId).pipe(
            tap((response: ApiResultVoid) => {
              if (response.success) {
                patchState(store, { loading: false });
                showMessage(response.message || 'Lote eliminado exitosamente', 'success');
                loadBatches();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al eliminar el lote', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al eliminar el lote');
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
        loadBatches();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), name: query, page: 0 }
        });
        loadBatches();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), name: '', page: 0 }
        });
        loadBatches();
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
        loadBatches();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: 'ASC' | 'DESC' }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadBatches();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadBatches();
      },

      create: rxMethod<CreateBatchRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._batchService.createBatch(request).pipe(
              tap((response: ApiResultCommandBatchResponse) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message || 'Lote creado exitosamente', 'success');
                  store.isCreateModalVisible.set(false);
                  loadBatches();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el lote', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear el lote');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateBatchRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request }) =>
            store._batchService.updateBatch(publicId, request).pipe(
              tap((response: ApiResultCommandBatchResponse) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message || 'Lote actualizado exitosamente', 'success');
                  store.isUpdateModalVisible.set(false);
                  loadBatches();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar el lote', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar el lote');
                return of(null);
              })
            )
          )
        )
      ),

      delete: deleteMethod,

      confirmAndDelete: (batch: BatchListDTO) => {
        confirmationService.confirm({
          message: `¿Estás seguro de que quieres eliminar el lote "${batch.name}"?`,
          header: 'Confirmar Eliminación',
          icon: 'pi pi-exclamation-triangle',
          acceptButtonStyleClass: 'p-button-danger',
          accept: () => {
            deleteMethod(batch.publicId);
          },
        });
      },

      refresh: () => {
        loadBatches();
        subsidiaryStore.init(); // Call subsidiary store's init method
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      getDetails: (publicId: string) => {
        return store._batchService.getBatchDetails(publicId).pipe(
          tap((response) => {
            if (!response.success) {
              handleHttpError(null, 'Error al cargar los detalles del lote', response.message);
            }
          }),
          catchError((err) => {
            handleHttpError(err, 'Error de conexión al cargar los detalles del lote');
            return of({ success: false, message: 'Error de conexión', data: null, timeStamp: new Date().toISOString() });
          })
        );
      },

      init: () => {
        subsidiaryStore.init(); // Call subsidiary store's init method
        loadBatches();
      },

      showCreateModal: () => {
        store.isCreateModalVisible.set(true);
      },

      hideCreateModal: () => {
        store.isCreateModalVisible.set(false);
      },
      showUpdateModal: (batch: BatchDetailsDTO) => {
        store.selectedBatch.set(batch);
        store.isUpdateModalVisible.set(true);
      },

      hideUpdateModal: () => {
        store.isUpdateModalVisible.set(false);
        store.selectedBatch.set(null);
      },

      getSelectOptions: () => {
        return store._batchService.getSelectOptions().pipe(
          catchError((err) => {
            handleHttpError(err, 'Error de conexión al cargar opciones de lotes');
            return of({ success: false, message: 'Error de conexión', data: [], timeStamp: new Date().toISOString() });
          })
        );
      }
    };
  })
);
