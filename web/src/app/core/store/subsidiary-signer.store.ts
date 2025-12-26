import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of, Observable } from 'rxjs';
import { SubsidiarySignerService, SubsidiarySignerListDTO, SubsidiarySignerDetailsDTO, CreateSubsidiarySignerRequest, UpdateSubsidiarySignerRequest } from '../services/subsidiary-signer.service';
import { EmployeeService } from '../services/employee.service';
import { InternalFileStore } from './internal-file.store';
import { MessageService } from 'primeng/api';
import { PagedResult } from '@shared/types/api';

interface SubsidiarySignerFilters {
  subsidiaryName: string;
  responsibleEmployeeName: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface SubsidiarySignerState {
  subsidiaries: SubsidiarySignerListDTO[];
  loading: boolean;
  error: string | null;
  uploadingImage: boolean;
  signatureImageUrl: string | null;
  signatureImageFile: File | null; 
  signatureImagePreview: string | null; 
  totalElements: number;
  filters: SubsidiarySignerFilters;
  currentSignerDetails: SubsidiarySignerDetailsDTO | null;
  employeePosition: string | null;
}

const initialState: SubsidiarySignerState = {
  subsidiaries: [],
  loading: false,
  error: null,
  uploadingImage: false,
  signatureImageUrl: null,
  signatureImageFile: null,
  signatureImagePreview: null,
  totalElements: 0,
  filters: {
    subsidiaryName: '',
    responsibleEmployeeName: '',
    page: 0,
    pageSize: 10,
    sortBy: 'subsidiaryName',
    sortDirection: 'ASC'
  },
  currentSignerDetails: null,
  employeePosition: null,
};

export const SubsidiarySignerStore = signalStore(
  { providedIn: 'root' },
  withState<SubsidiarySignerState>(initialState),

  withProps(() => ({
    _signerService: inject(SubsidiarySignerService),
    _messageService: inject(MessageService),
    _employeeService: inject(EmployeeService),
    _internalFileStore: inject(InternalFileStore)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.subsidiaries().length === 0),
    hasSubsidiaries: computed(() => state.subsidiaries().length > 0),
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
      patchState(store, { loading: false, error: messages.join('\n') });
    };

    const loadSubsidiaries = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          return store._signerService.listSubsidiariesWithSignersPaged({
            subsidiaryName: filters.subsidiaryName || undefined,
            responsibleEmployeeName: filters.responsibleEmployeeName || undefined,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          }).pipe(
            tap((response) => {
              if (response.success) {
                const subsidiaries = response.data.data;
                
                patchState(store, { 
                  subsidiaries: subsidiaries, 
                  totalElements: response.data.totalElements,
                  loading: false 
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar subsidiarias', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar subsidiarias');
              return of(null);
            })
          );
        })
      )
    );

    const createSigner = rxMethod<CreateSubsidiarySignerRequest>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((request) => {
          const imageFile = store.signatureImageFile();
          return store._signerService.createSigner(request, imageFile || undefined).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Responsable de firma asignado correctamente', 'success');
                patchState(store, { 
                  loading: false, 
                  signatureImageUrl: null,
                  signatureImageFile: null,
                  signatureImagePreview: null
                });
                loadSubsidiaries();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al asignar responsable de firma', response.message);
              }
            }),
            catchError((err) => {
              patchState(store, { loading: false });
              handleHttpError(err, 'Error de conexión al asignar responsable de firma');
              return of(null);
            })
          );
        })
      )
    );

    const deleteSigner = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((subsidiaryPublicId) => store._signerService.deleteSigner(subsidiaryPublicId).pipe(
          tap((response) => {
            if (response.success) {
              showMessage('Responsable de firma eliminado correctamente', 'success');
              patchState(store, { loading: false });
              loadSubsidiaries();
            } else {
              patchState(store, { loading: false });
              handleHttpError(null, 'Error al eliminar responsable de firma', response.message);
            }
          }),
          catchError((err) => {
            handleHttpError(err, 'Error de conexión al eliminar responsable de firma');
            return of(null);
          })
        ))
      )
    );

    const setSignatureImageFile = (file: File | null) => {
      if (store.signatureImagePreview()) {
        URL.revokeObjectURL(store.signatureImagePreview()!);
      }
      
      if (file) {
          if (!file.type.startsWith('image/')) {
            showMessage('El archivo debe ser una imagen', 'error');
          return;
          }

          if (file.size > 1000000) {
            showMessage('La imagen no puede exceder 1MB', 'error');
          return;
        }

        const previewUrl = URL.createObjectURL(file);
        patchState(store, { 
          signatureImageFile: file, 
          signatureImagePreview: previewUrl
        });
      } else {
        patchState(store, { 
          signatureImageFile: null, 
          signatureImagePreview: null,
          signatureImageUrl: null
        });
      }
    };

    const uploadSignatureImage = (file: File): Observable<string | null> => {
      const tempUrl = URL.createObjectURL(file);
      return of(tempUrl);
    };

    const updateSigner = rxMethod<{ publicId: string; request: UpdateSubsidiarySignerRequest }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(({ publicId, request }) => {
          const imageFile = store.signatureImageFile();
          return store._signerService.updateSigner(publicId, request, imageFile || undefined).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Responsable de firma actualizado correctamente', 'success');
                patchState(store, { 
                  loading: false, 
                  signatureImageUrl: null,
                  signatureImageFile: null,
                  signatureImagePreview: null
                });
                loadSubsidiaries();
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al actualizar responsable de firma', response.message);
              }
            }),
            catchError((err) => {
              patchState(store, { loading: false });
              handleHttpError(err, 'Error de conexión al actualizar responsable de firma');
              return of(null);
            })
          );
        })
      )
    );

    const getSignerBySubsidiary = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((subsidiaryPublicId) => store._signerService.getSignerBySubsidiary(subsidiaryPublicId).pipe(
          tap((response) => {
            if (response.success) {
              patchState(store, { 
                currentSignerDetails: response.data, 
                loading: false 
              });
            } else {
              patchState(store, { loading: false, currentSignerDetails: null });
              handleHttpError(null, 'Error al obtener responsable de firma', response.message);
            }
          }),
          catchError((err) => {
            patchState(store, { loading: false, currentSignerDetails: null });
            handleHttpError(err, 'Error de conexión al obtener responsable de firma');
            return of(null);
          })
        ))
      )
    );

    const searchEmployeePosition = rxMethod<string>(
      pipe(
        switchMap((documentNumber) => store._employeeService.searchByDocumentNumber(documentNumber).pipe(
          tap((response) => {
            if (response.success && response.data) {
              const position = response.data.position || 'JEFE DE RECURSOS HUMANOS';
              patchState(store, { employeePosition: position });
            } else {
              patchState(store, { employeePosition: 'JEFE DE RECURSOS HUMANOS' });
            }
          }),
          catchError((err) => {
            patchState(store, { employeePosition: 'JEFE DE RECURSOS HUMANOS' });
            return of(null);
          })
        ))
      )
    );

    return {
      subsidiaries: store.subsidiaries,
      loading: store.loading,
      error: store.error,
      isEmpty: store.isEmpty,
      hasSubsidiaries: store.hasSubsidiaries,
      uploadingImage: store.uploadingImage,
      signatureImageUrl: store.signatureImageUrl,
      signatureImageFile: store.signatureImageFile,
      signatureImagePreview: store.signatureImagePreview,
      totalElements: store.totalElements,
      filters: store.filters,
      currentSignerDetails: store.currentSignerDetails,
      employeePosition: store.employeePosition,
      loadSubsidiaries,
      createSigner,
      updateSigner,
      deleteSigner,
      uploadSignatureImage,
      getSignerBySubsidiary,
      searchEmployeePosition,
      setSignatureImageUrl: (url: string | null) => {
        if (url) {
          const authenticatedUrl = store._internalFileStore.getAuthenticatedUrl(url);
          patchState(store, { signatureImageUrl: authenticatedUrl });
        } else {
          patchState(store, { signatureImageUrl: null });
        }
      },
      setSignatureImageFile,
      clearSignatureImage: () => {
        if (store.signatureImagePreview()) {
          URL.revokeObjectURL(store.signatureImagePreview()!);
        }
        const currentUrl = store.signatureImageUrl();
        if (currentUrl && currentUrl.startsWith('blob:')) {
          store._internalFileStore.revokeBlobUrl(currentUrl);
        }
        patchState(store, { 
          signatureImageUrl: null, 
          signatureImageFile: null,
          signatureImagePreview: null
        });
      },
      clearEmployeePosition: () => patchState(store, { employeePosition: null }),
      clearSignerDetails: () => patchState(store, { currentSignerDetails: null }),
      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), subsidiaryName: query, page: 0 }
        });
        loadSubsidiaries();
      },
      filterByResponsible: (responsibleName: string) => {
        patchState(store, {
          filters: { ...store.filters(), responsibleEmployeeName: responsibleName, page: 0 }
        });
        loadSubsidiaries();
      },
      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), subsidiaryName: '', responsibleEmployeeName: '', page: 0 }
        });
        loadSubsidiaries();
      },
      resetFilters: () => {
        patchState(store, {
          filters: {
            subsidiaryName: '',
            responsibleEmployeeName: '',
            page: 0,
            pageSize: 10,
            sortBy: 'subsidiaryName',
            sortDirection: 'ASC'
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
      clearError: () => patchState(store, { error: null }),
      getImageBlobUrl: (url: string | null | undefined): string | null => {
        return store._internalFileStore.getAuthenticatedUrl(url);
      },
      isImageLoading: (url: string | null | undefined): boolean => {
        return store._internalFileStore.isLoading(url);
      },
      clearImageBlobUrls: () => {
      },
    };
  })
);

