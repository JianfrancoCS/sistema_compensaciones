import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { Element, CreateElementRequest, UpdateElementRequest, ElementParams } from '@shared/types/element';
import { ElementService } from '../services/element.service';
import { ImageService, Bucket } from '../services/image.service';
import { MessageService } from 'primeng/api';

interface ElementFilters {
  query: string;
  containerPublicId: string | null;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface ElementState {
  elements: Element[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  filters: ElementFilters;
}

const initialState: ElementState = {
  elements: [],
  loading: false,
  error: null,
  totalElements: 0,
  filters: {
    query: '',
    containerPublicId: null,
    page: 0,
    pageSize: 10,
    sortBy: 'orderIndex',
    sortDirection: 'ASC'
  }
};

export const ElementStore = signalStore(
  { providedIn: 'root' },
  withState<ElementState>(initialState),

  withProps(() => ({
    _elementService: inject(ElementService),
    _imageService: inject(ImageService),
    _messageService: inject(MessageService)
  })),

  withComputed((store) => ({
    isEmpty: computed(() => !store.loading() && store.elements().length === 0)
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

    const loadElements = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: ElementParams = {
            query: filters.query,
            containerPublicId: filters.containerPublicId || undefined,
            page: filters.page,
            pageSize: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._elementService.getElements(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  elements: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar elementos', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar elementos');
              return of(null);
            })
          );
        })
      )
    );

    return {
      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), query, page: 0 }
        });
        loadElements();
      },

      filterByContainer: (containerPublicId: string | null) => {
        patchState(store, {
          filters: { ...store.filters(), containerPublicId, page: 0 }
        });
        loadElements();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), query: '', containerPublicId: null, page: 0 }
        });
        loadElements();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            query: '',
            containerPublicId: null,
            page: 0,
            pageSize: 10,
            sortBy: 'orderIndex',
            sortDirection: 'ASC'
          }
        });
        loadElements();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadElements();
      },
      onLazyLoad: (event: any) => {
        const page = (event.first ?? 0) / (event.rows ?? 10);
        const pageSize = event.rows ?? 10;

        let sortBy = 'orderIndex';
        if (event.sortField) {
          if (Array.isArray(event.sortField)) {
            sortBy = event.sortField[0] || 'orderIndex';
          } else {
            sortBy = event.sortField;
          }
        }

        const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

        patchState(store, {
          filters: { ...store.filters(), page, pageSize, sortBy, sortDirection }
        });
        loadElements();
      },

      create: rxMethod<CreateElementRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._elementService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Elemento creado exitosamente', 'success');
                  loadElements();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el elemento', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear el elemento');
                return of(null);
              })
            )
          )
        )
      ),

      createWithImage: rxMethod<{ request: CreateElementRequest; file: File | null }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ request, file }) => {
            if (file) {
              return store._imageService.uploadToCloudinary(file, Bucket.MENU_ICON).pipe(
                switchMap((iconUrl) => {
                  const requestWithImage: CreateElementRequest = {
                    ...request,
                    iconUrl: iconUrl
                  };
                  return store._elementService.create(requestWithImage).pipe(
                    tap((response) => {
                      if (response.success) {
                        patchState(store, { loading: false });
                        showMessage('Elemento creado exitosamente', 'success');
                        loadElements();
                      } else {
                        patchState(store, { loading: false });
                        handleHttpError(null, 'Error al crear el elemento', response.message);
                      }
                    }),
                    catchError((err) => {
                      handleHttpError(err, 'Error al crear el elemento');
                      return of(null);
                    })
                  );
                }),
                catchError((err) => {
                  handleHttpError(err, 'Error al subir la imagen');
                  return of(null);
                })
              );
            } else {
              return store._elementService.create(request).pipe(
                tap((response) => {
                  if (response.success) {
                    patchState(store, { loading: false });
                    showMessage('Elemento creado exitosamente', 'success');
                    loadElements();
                  } else {
                    patchState(store, { loading: false });
                    handleHttpError(null, 'Error al crear el elemento', response.message);
                  }
                }),
                catchError((err) => {
                  handleHttpError(err, 'Error al crear el elemento');
                  return of(null);
                })
              );
            }
          })
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateElementRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((params) =>
            store._elementService.update(params.publicId, params.request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Elemento actualizado exitosamente', 'success');
                  loadElements();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar el elemento', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar el elemento');
                return of(null);
              })
            )
          )
        )
      ),

      updateWithImage: rxMethod<{ publicId: string; request: UpdateElementRequest; file: File | null }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request, file }) => {
            if (file) {
              return store._imageService.uploadToCloudinary(file, Bucket.MENU_ICON).pipe(
                switchMap((iconUrl) => {
                  const requestWithImage: UpdateElementRequest = {
                    ...request,
                    iconUrl: iconUrl
                  };
                  return store._elementService.update(publicId, requestWithImage).pipe(
                    tap((response) => {
                      if (response.success) {
                        patchState(store, { loading: false });
                        showMessage('Elemento actualizado exitosamente', 'success');
                        loadElements();
                      } else {
                        patchState(store, { loading: false });
                        handleHttpError(null, 'Error al actualizar el elemento', response.message);
                      }
                    }),
                    catchError((err) => {
                      handleHttpError(err, 'Error al actualizar el elemento');
                      return of(null);
                    })
                  );
                }),
                catchError((err) => {
                  handleHttpError(err, 'Error al subir la imagen');
                  return of(null);
                })
              );
            } else {
              return store._elementService.update(publicId, request).pipe(
                tap((response) => {
                  if (response.success) {
                    patchState(store, { loading: false });
                    showMessage('Elemento actualizado exitosamente', 'success');
                    loadElements();
                  } else {
                    patchState(store, { loading: false });
                    handleHttpError(null, 'Error al actualizar el elemento', response.message);
                  }
                }),
                catchError((err) => {
                  handleHttpError(err, 'Error al actualizar el elemento');
                  return of(null);
                })
              );
            }
          })
        )
      ),

      delete: rxMethod<string>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((publicId) =>
            store._elementService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Elemento eliminado exitosamente', 'success');
                  loadElements();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar el elemento', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar el elemento');
                return of(null);
              })
            )
          )
        )
      ),

      getForUpdate: (publicId: string) => {
        return store._elementService.getForUpdate(publicId);
      },

      refresh: () => {
        loadElements();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      init: () => {
        loadElements();
      }
    };
  })
);

