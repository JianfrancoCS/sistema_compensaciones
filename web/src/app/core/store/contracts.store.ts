import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { ContractService } from '../services/contract.service';
import { ContractTypeService } from '../services/contract-type.service';
import { InternalFileStore } from './internal-file.store';
import { ContractParams } from '@shared/types/contract';
import { HttpClient } from '@angular/common/http';
import { MessageService } from 'primeng/api'; // Import MessageService
import { Router } from '@angular/router'; // Import Router
import {
  ContractListDTO,
  CreateContractRequest,
  UpdateContractRequest,
  CancelContractRequest,
  SignContractRequest,
  CommandContractResponse
} from '@shared/types/contract';

export interface ContractFilters {
  contractNumber: string;
  contractTypePublicId: string;
  statePublicId: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export interface ContractState {
  contracts: ContractListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  filters: ContractFilters;
  isSubmitting: boolean; // Add isSubmitting state
}

const initialState: ContractState = {
  contracts: [],
  loading: false,
  error: null,
  totalElements: 0,
  filters: {
    contractNumber: '',
    contractTypePublicId: '',
    statePublicId: '',
    page: 0,
    pageSize: 10,
    sortBy: 'updatedAt',
    sortDirection: 'DESC'
  },
  isSubmitting: false // Initialize isSubmitting
};

export const ContractStore = signalStore(
  withState<ContractState>(initialState),

  withProps(() => ({
    _contractService: inject(ContractService),
    _contractTypeService: inject(ContractTypeService),
    _internalFileStore: inject(InternalFileStore),
    _http: inject(HttpClient), // Solo para uploads a Cloudinary
    _messageService: inject(MessageService),
    _router: inject(Router)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.contracts().length === 0)
  })),

  withMethods((store) => {
    const showToast = (severity: 'success' | 'error' | 'info' | 'warn', summary: string, detail: string) => {
      store._messageService.add({ severity, summary, detail });
    };

    const getErrorMessage = (err: any, defaultMessage: string): string => {
      let errorMessage = defaultMessage;
      if (err?.error?.message) {
        errorMessage = err.error.message;
      } else if (err?.message) {
        errorMessage = err.message;
      }
      showToast('error', 'Error', errorMessage); // Show toast for errors
      return errorMessage;
    };

    const loadContracts = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: ContractParams = {
            contractNumber: filters.contractNumber,
            contractTypePublicId: filters.contractTypePublicId,
            statePublicId: filters.statePublicId,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._contractService.getContracts(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  contracts: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                showToast('error', 'Error', response.message || 'Error al cargar contratos');
                patchState(store, { loading: false, error: response.message });
              }
            }),
            catchError((err) => {
              getErrorMessage(err, 'Error de conexión al cargar contratos');
              patchState(store, { loading: false, error: 'Error de conexión al cargar contratos' });
              return of(null);
            })
          );
        })
      )
    );

    const update = rxMethod<{ publicId: string; request: UpdateContractRequest }>(
      pipe(
        tap(() => patchState(store, { isSubmitting: true, error: null })),
        switchMap(({ publicId, request }) =>
          store._contractService.update(publicId, request).pipe(
            tap((response) => {
              patchState(store, { isSubmitting: false });
              if (response.success) {
                loadContracts();
                showToast('success', 'Éxito', 'Contrato actualizado correctamente');
                store._router.navigate(['/system/contracts']);
              } else {
                showToast('error', 'Error', response.message || 'Error al actualizar el contrato');
                patchState(store, { error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error de conexión al actualizar el contrato');
              patchState(store, { isSubmitting: false, error });
              return of(null);
            })
          )
        )
      )
    );

    return {
      updateFilter: (filters: Partial<ContractFilters>) => {
        patchState(store, {
          filters: { ...store.filters(), ...filters, page: 0 }
        });
        loadContracts();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), contractNumber: query, page: 0 }
        });
        loadContracts();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), contractNumber: '', page: 0 }
        });
        loadContracts();
      },

      filterByContractType: (contractTypePublicId: string) => {
        patchState(store, {
          filters: { ...store.filters(), contractTypePublicId, page: 0 }
        });
        loadContracts();
      },

      filterByState: (statePublicId: string) => {
        patchState(store, {
          filters: { ...store.filters(), statePublicId, page: 0 }
        });
        loadContracts();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            contractNumber: '',
            contractTypePublicId: '',
            statePublicId: '',
            page: 0,
            pageSize: 10,
            sortBy: 'updatedAt',
            sortDirection: 'DESC'
          }
        });
        loadContracts();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadContracts();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadContracts();
      },

      update: update, // Use the new rxMethod

      cancel: (params: { publicId: string; request: CancelContractRequest }) => {
        const { publicId, request } = params;
        return store._contractService.cancelContract(publicId, request).pipe(
          tap((response) => {
            if (response.success) {
              loadContracts();
              showToast('success', 'Éxito', 'Contrato cancelado correctamente');
            } else {
              showToast('error', 'Error', response.message || 'Error al cancelar el contrato');
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al cancelar el contrato');
            patchState(store, { error: 'Error de conexión al cancelar el contrato' });
            return of(null);
          })
        );
      },

      refresh: () => {
        loadContracts();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      init: () => {
        loadContracts();
      },

      generateUploadUrl: (contractId: string, request: { fileName: string }) => {
        return store._contractService.generateUploadUrl(contractId, request).pipe(
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al generar URL de carga');
            return of({ success: false, message: 'Error de conexión al generar URL de carga', data: null });
          })
        );
      },

      attachFile: (contractId: string, request: { imagesUri: string[] }) => {
        return store._contractService.attachFile(contractId, request).pipe(
          tap((response) => {
            if (response.success) {
              loadContracts(); // Recargar la lista después de adjuntar archivo
              showToast('success', 'Éxito', 'Archivo adjuntado correctamente');
            } else {
              showToast('error', 'Error', response.message || 'Error al adjuntar archivo');
            }
          }),
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al adjuntar archivo');
            return of({ success: false, message: 'Error de conexión al adjuntar archivo', data: null });
          })
        );
      },

      getContractForCommand: (publicId: string) => {
        return store._contractService.getContractForCommand(publicId).pipe(
          catchError((err) => {
            getErrorMessage(err, 'Error al cargar el contrato para edición');
            store._router.navigate(['/system/contracts']); // Navigate on error
            return of({ success: false, message: 'Error al cargar el contrato para edición', data: null });
          })
        );
      },

      getStatesSelectOptions: () => {
        return store._contractService.getStatesSelectOptions().pipe(
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al obtener estados');
            return of({ success: false, message: 'Error de conexión al obtener estados', data: [] });
          })
        );
      },

      getContractTypeSelectOptions: () => {
        return store._contractTypeService.getContractTypeSelectOptions().pipe(
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al obtener tipos de contrato');
            return of({ success: false, message: 'Error de conexión al obtener tipos de contrato', data: [] });
          })
        );
      },

      getContractContent: (publicId: string) => {
        return store._contractService.getContractContent(publicId).pipe(
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al obtener contenido del contrato');
            return of({ success: false, message: 'Error de conexión al obtener contenido del contrato', data: null });
          })
        );
      },


      signContract: (publicId: string, signatureFile: File) => {
        return store._contractService.signContract(publicId, signatureFile).pipe(
          tap((response) => {
            if (response.success) {
              loadContracts(); // Recargar la lista después de firmar
              showToast('success', 'Éxito', 'Contrato firmado correctamente');
            } else {
              showToast('error', 'Error', response.message || 'Error al firmar el contrato');
            }
          }),
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al firmar el contrato');
            return of({ success: false, message: 'Error de conexión al firmar el contrato', data: null });
          })
        );
      },

      cancelContract: (publicId: string, request: CancelContractRequest) => {
        return store._contractService.cancelContract(publicId, request).pipe(
          tap((response) => {
            if (response.success) {
              loadContracts(); // Recargar la lista después de cancelar
              showToast('success', 'Éxito', 'Contrato anulado correctamente');
            } else {
              showToast('error', 'Error', response.message || 'Error al anular el contrato');
            }
          }),
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al anular el contrato');
            return of({ success: false, message: 'Error de conexión al anular el contrato', data: null });
          })
        );
      },

      getContractDetails: (publicId: string) => {
        return store._contractService.getContractDetails(publicId).pipe(
          catchError((err) => {
            getErrorMessage(err, 'Error de conexión al obtener detalles del contrato');
            return of({ success: false, message: 'Error de conexión al obtener detalles del contrato', data: null });
          })
        );
      },

      uploadAndAttachFile: (contractPublicId: string, file: File) => {
        return store._contractService.uploadFile(contractPublicId, file).pipe(
          tap((response) => {
            if (response.success) {
              loadContracts();
              showToast('success', 'Éxito', 'Archivo subido y adjuntado correctamente');
            } else {
              showToast('error', 'Error', response.message || 'Error al subir archivo');
            }
          }),
          catchError((err) => {
            getErrorMessage(err, 'Error al subir y adjuntar archivo');
            return of({ success: false, message: 'Error al subir y adjuntar archivo', data: null });
          })
        );
      },

      uploadAndAttachMultipleFiles: (contractId: string, files: File[]) => {
        const uploadPromises = files.map((file, index) =>
          store._contractService.generateUploadUrl(contractId, { fileName: file.name }).pipe(
            switchMap((res) => {
              if (res.success && res.data) {
                const { uploadUrl, apiKey, timestamp, signature, folder } = res.data;
                const formData = new FormData();
                formData.append('file', file);
                formData.append('api_key', apiKey);
                formData.append('timestamp', timestamp.toString());
                formData.append('signature', signature);
                formData.append('folder', folder);

                return store._http.post(uploadUrl, formData).pipe(
                  switchMap((uploadRes: any) => of({ url: uploadRes.secure_url, order: index }))
                );
              } else {
                showToast('error', 'Error', res.message || 'Error al generar URL de carga para archivo ' + file.name);
                return of({ error: res.message, order: index });
              }
            })
          ).toPromise()
        );

        return Promise.all(uploadPromises).then((results) => {
          const orderedUrls = results
            .filter((result: any) => result.url)
            .sort((a: any, b: any) => a.order - b.order)
            .map((result: any) => result.url);

          if (orderedUrls.length === files.length) {
            return store._contractService.attachFile(contractId, { imagesUri: orderedUrls }).pipe(
              tap((response) => {
                if (response.success) {
                  loadContracts();
                  showToast('success', 'Éxito', 'Múltiples archivos adjuntados correctamente');
                } else {
                  showToast('error', 'Error', response.message || 'Error al adjuntar múltiples archivos');
                }
              })
            );
          } else {
            showToast('error', 'Error', 'Error al subir algunos archivos');
            return of({ success: false, message: 'Error al subir algunos archivos', data: null });
          }
        }).catch((err) => {
          getErrorMessage(err, 'Error al subir múltiples archivos');
          return of({ success: false, message: 'Error al subir múltiples archivos', data: null });
        });
      },

      navigateToContracts: () => {
        store._router.navigate(['/system/contracts']);
      },

      navigateToCreateContract: () => {
        store._router.navigate(['/system/contracts/create']);
      },

      navigateToEditContract: (publicId: string) => {
        store._router.navigate(['/system/contracts/edit', publicId]);
      },
      getAuthenticatedFileBlobUrl: (url: string | null | undefined) => {
        return store._internalFileStore.getBlobUrlObservable(url);
      },
      revokeBlobUrl: (blobUrl: string | null) => {
        store._internalFileStore.revokeBlobUrl(blobUrl);
      },
      
      showToast: (severity: 'success' | 'error' | 'info' | 'warn', summary: string, detail: string) => {
        showToast(severity, summary, detail);
      },
    };
  })
);
