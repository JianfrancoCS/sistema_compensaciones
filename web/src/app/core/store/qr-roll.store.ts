import { inject, computed } from '@angular/core';
import { patchState, signalStore, withMethods, withState, withProps, withComputed } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { QrRollService } from '@core/services/qr-roll.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  AssignRollToEmployeeRequest,
  BatchGenerateQrCodesRequest,
  CreateQrRollRequest,
  GenerateQrCodesRequest,
  QrCodeDTO,
  QrCodeFilters,
  QrRollListDTO,
  QrRollPageableRequest,
  UpdateQrRollRequest
} from "@shared/types/qr-roll";

export interface QrRollState {
  qrRolls: QrRollListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  filters: Partial<QrRollPageableRequest>;
  isCreateModalVisible: boolean;
  isEditModalVisible: boolean;
  selectedQrRoll: QrRollListDTO | null;
  qrCodes: QrCodeDTO[];
  qrCodesLoading: boolean;
  qrCodesError: string | null;
  qrCodeFilters: Partial<QrCodeFilters>;
}

const initialState: QrRollState = {
  qrRolls: [],
  loading: false,
  error: null,
  totalElements: 0,
  filters: {
    page: 0,
    size: 10,
    sortBy: 'createdAt',
    sortDirection: 'desc',
    hasUnprintedCodes: undefined,
  },
  isCreateModalVisible: false,
  isEditModalVisible: false,
  selectedQrRoll: null,
  qrCodes: [],
  qrCodesLoading: false,
  qrCodesError: null,
  qrCodeFilters: {
    rollPublicId: undefined,
    isUsed: undefined,
    isPrinted: undefined
  }
};

export const QrRollStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withProps(() => ({
    _qrRollService: inject(QrRollService),
    _messageService: inject(MessageService),
    _confirmationService: inject(ConfirmationService),
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.qrRolls().length === 0)
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
      patchState(store, { error: errorForState });
    };

    const loadQrRolls = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: QrRollPageableRequest = {
            page: filters.page ?? 0,
            size: filters.size ?? 10,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection,
            hasUnprintedCodes: filters.hasUnprintedCodes
          };

          return store._qrRollService.getAll(params).pipe(
            tap((response: ApiResult<PagedResult<QrRollListDTO>>) => {
              if (response.success) {
                patchState(store, {
                  qrRolls: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar los rollos QR', response.message);
              }
            }),
            catchError((err) => {
              patchState(store, { loading: false });
              handleHttpError(err, 'Error de conexión al cargar los rollos QR');
              return of(null);
            })
          );
        })
      )
    );

    const updateQrRoll = rxMethod<{ publicId: string; request: UpdateQrRollRequest }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(({ publicId, request }) =>
          store._qrRollService.update(publicId, request).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { loading: false, isEditModalVisible: false, selectedQrRoll: null });
                showMessage(response.message, 'success');
                loadQrRolls();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al actualizar el rollo QR', response.message);
              }
            }),
            catchError((err) => {
              patchState(store, { loading: false });
              handleHttpError(err, 'Error de conexión al actualizar el rollo QR');
              return of(null);
            })
          )
        )
      )
    );

    const deleteQrRoll = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((publicId) =>
          store._qrRollService.delete(publicId).pipe(
            tap((response: ApiResult<void>) => {
              if (response.success) {
                patchState(store, { loading: false });
                showMessage(response.message, 'success');
                loadQrRolls();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al eliminar el rollo QR', response.message);
              }
            }),
            catchError((err) => {
              patchState(store, { loading: false });
              handleHttpError(err, 'Error de conexión al eliminar el rollo QR');
              return of(null);
            })
          )
        )
      )
    );

    const printQrCodes = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((rollPublicId) =>
          store._qrRollService.print(rollPublicId).pipe(
            tap((response: ApiResult<void>) => {
              if (response.success) {
                patchState(store, { loading: false });
                showMessage(response.message, 'success');
                loadQrRolls();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al marcar los códigos como impresos', response.message);
              }
            }),
            catchError((err) => {
              patchState(store, { loading: false });
              handleHttpError(err, 'Error de conexión al imprimir los códigos');
              return of(null);
            })
          )
        )
      )
    );

    const loadQrCodes = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { qrCodesLoading: true, qrCodesError: null })),
        switchMap(() => {
          const filters = store.qrCodeFilters();
          if (!filters.rollPublicId) {
            return of(null);
          }
          const qrCodeParams: QrCodeFilters = {
            rollPublicId: filters.rollPublicId,
            isUsed: filters.isUsed,
            isPrinted: filters.isPrinted
          };
          return store._qrRollService.getQrCodes(qrCodeParams).pipe(
            tap((response: ApiResult<QrCodeDTO[]>) => {
              if (response.success) {
                patchState(store, { qrCodes: response.data, qrCodesLoading: false });
              } else {
                patchState(store, { qrCodesLoading: false });
                handleHttpError(null, 'Error al cargar los códigos QR', response.message);
              }
            }),
            catchError((err) => {
              patchState(store, { qrCodesLoading: false });
              handleHttpError(err, 'Error de conexión al cargar los códigos QR');
              return of(null);
            })
          );
        })
      )
    );

    return {
      openCreateModal: () => patchState(store, { isCreateModalVisible: true }),
      closeCreateModal: () => patchState(store, { isCreateModalVisible: false }),
      openEditModal: (qrRoll: QrRollListDTO) => patchState(store, { isEditModalVisible: true, selectedQrRoll: qrRoll }),
      closeEditModal: () => patchState(store, { isEditModalVisible: false, selectedQrRoll: null }),

      setFilters(filters: Partial<QrRollPageableRequest>) {
        patchState(store, { filters: { ...store.filters(), ...filters, page: 0 } });
        loadQrRolls();
      },

      loadPage: (params: { page: number; size: number; sortBy?: string; sortDirection?: 'asc' | 'desc' }) => {
        patchState(store, { filters: { ...store.filters(), ...params } });
        loadQrRolls();
      },

      create: rxMethod<CreateQrRollRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._qrRollService.create(request).pipe(
              tap((response: ApiResult<any>) => {
                if (response.success) {
                  patchState(store, { loading: false, isCreateModalVisible: false });
                  showMessage(response.message, 'success');
                  loadQrRolls();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el rollo QR', response.message);
                }
              }),
              catchError((err) => {
                patchState(store, { loading: false });
                handleHttpError(err, 'Error de conexión al crear el rollo QR');
                return of(null);
              })
            )
          )
        )
      ),

      update: updateQrRoll,

      generateCodes: rxMethod<{ rollPublicId: string; request: GenerateQrCodesRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ rollPublicId, request }) =>
            store._qrRollService.generateCodes(rollPublicId, request).pipe(
              tap((response: ApiResult<void>) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadQrRolls();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al generar los códigos QR', response.message);
                }
              }),
              catchError((err) => {
                patchState(store, { loading: false });
                handleHttpError(err, 'Error de conexión al generar los códigos');
                return of(null);
              })
            )
          )
        )
      ),

      batchGenerate: rxMethod<BatchGenerateQrCodesRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._qrRollService.batchGenerate(request).pipe(
              tap((response: ApiResult<void>) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadQrRolls();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al generar los rollos en lote', response.message);
                }
              }),
              catchError((err) => {
                patchState(store, { loading: false });
                handleHttpError(err, 'Error de conexión al generar en lote');
                return of(null);
              })
            )
          )
        )
      ),

      assignToEmployee: rxMethod<AssignRollToEmployeeRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._qrRollService.assignToEmployee(request).pipe(
              tap((response: ApiResult<void>) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message, 'success');
                  loadQrRolls();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al asignar el rollo', response.message);
                }
              }),
              catchError((err) => {
                patchState(store, { loading: false });
                handleHttpError(err, 'Error de conexión al asignar el rollo');
                return of(null);
              })
            )
          )
        )
      ),

      delete: deleteQrRoll,

      confirmDelete: (qrRoll: QrRollListDTO) => {
        store._confirmationService.confirm({
          message: `¿Estás seguro de que quieres eliminar el rollo QR creado el ${new Date(qrRoll.createdAt).toLocaleDateString()}?`,
          header: 'Confirmar Eliminación',
          icon: 'pi pi-exclamation-triangle',
          accept: () => deleteQrRoll(qrRoll.publicId),
        });
      },

      confirmPrint: (qrRoll: QrRollListDTO) => {
        store._confirmationService.confirm({
          message: `Se marcarán como impresos ${qrRoll.unprintedQrCodes} código(s) QR del rollo creado el ${new Date(qrRoll.createdAt).toLocaleDateString()}. ¿Continuar?`,
          header: 'Confirmar Impresión',
          icon: 'pi pi-print',
          accept: () => printQrCodes(qrRoll.publicId),
        });
      },

      refresh: () => loadQrRolls(),
      clearError: () => patchState(store, { error: null }),
      resetFilters: () => {
        patchState(store, {
          filters: {
            page: 0,
            size: 10,
            sortBy: 'createdAt',
            sortDirection: 'desc',
            hasUnprintedCodes: undefined
          }
        });
        loadQrRolls();
      },
      init: () => loadQrRolls(),

      setQrCodeFilters(filters: Partial<Omit<QrCodeFilters, 'rollPublicId'>>) {
        patchState(store, { qrCodeFilters: { ...store.qrCodeFilters(), ...filters } });
        loadQrCodes();
      },

      loadQrCodesForRoll(rollPublicId: string) {
        patchState(store, { qrCodeFilters: { rollPublicId, isUsed: undefined, isPrinted: undefined } });
        loadQrCodes();
      },

      refreshQrCodes: () => loadQrCodes(),

      clearQrCodeFilters() {
        const rollPublicId = store.qrCodeFilters().rollPublicId;
        patchState(store, { qrCodeFilters: { rollPublicId, isUsed: undefined, isPrinted: undefined } });
        loadQrCodes();
      }
    };
  })
);
