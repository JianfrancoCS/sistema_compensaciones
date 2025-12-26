import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { Container, CreateContainerRequest, UpdateContainerRequest, ContainerParams } from '@shared/types/container';
import { ContainerService } from '../services/container.service';
import { ImageService, Bucket } from '../services/image.service';
import { MessageService } from 'primeng/api';

interface ContainerFilters {
  query: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface ContainerState {
  containers: Container[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  filters: ContainerFilters;
}

const initialState: ContainerState = {
  containers: [],
  loading: false,
  error: null,
  totalElements: 0,
  filters: {
    query: '',
    page: 0,
    pageSize: 10,
    sortBy: 'orderIndex',
    sortDirection: 'ASC'
  }
};

export const ContainerStore = signalStore(
  { providedIn: 'root' },
  withState<ContainerState>(initialState),

  withProps(() => ({
    _containerService: inject(ContainerService),
    _imageService: inject(ImageService),
    _messageService: inject(MessageService)
  })),

  withComputed((store) => ({
    isEmpty: computed(() => !store.loading() && store.containers().length === 0)
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

    const loadContainers = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: ContainerParams = {
            query: filters.query,
            page: filters.page,
            pageSize: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._containerService.getContainers(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  containers: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar contenedores', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar contenedores');
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
        loadContainers();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), query: '', page: 0 }
        });
        loadContainers();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            query: '',
            page: 0,
            pageSize: 10,
            sortBy: 'orderIndex',
            sortDirection: 'ASC'
          }
        });
        loadContainers();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadContainers();
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
        loadContainers();
      },

      create: rxMethod<CreateContainerRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._containerService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Contenedor creado exitosamente', 'success');
                  loadContainers();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el contenedor', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear el contenedor');
                return of(null);
              })
            )
          )
        )
      ),

      createWithImage: rxMethod<{ request: CreateContainerRequest; file: File | null }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ request, file }) => {
            if (file) {
              return store._imageService.uploadToCloudinary(file, Bucket.MENU_ICON).pipe(
                switchMap((iconUrl) => {
                  const requestWithImage: CreateContainerRequest = {
                    ...request,
                    iconUrl: iconUrl
                  };
                  return store._containerService.create(requestWithImage).pipe(
                    tap((response) => {
                      if (response.success) {
                        patchState(store, { loading: false });
                        showMessage('Contenedor creado exitosamente', 'success');
                        loadContainers();
                      } else {
                        patchState(store, { loading: false });
                        handleHttpError(null, 'Error al crear el contenedor', response.message);
                      }
                    }),
                    catchError((err) => {
                      handleHttpError(err, 'Error al crear el contenedor');
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
              return store._containerService.create(request).pipe(
                tap((response) => {
                  if (response.success) {
                    patchState(store, { loading: false });
                    showMessage('Contenedor creado exitosamente', 'success');
                    loadContainers();
                  } else {
                    patchState(store, { loading: false });
                    handleHttpError(null, 'Error al crear el contenedor', response.message);
                  }
                }),
                catchError((err) => {
                  handleHttpError(err, 'Error al crear el contenedor');
                  return of(null);
                })
              );
            }
          })
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateContainerRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((params) =>
            store._containerService.update(params.publicId, params.request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Contenedor actualizado exitosamente', 'success');
                  loadContainers();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar el contenedor', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar el contenedor');
                return of(null);
              })
            )
          )
        )
      ),

      updateWithImage: rxMethod<{ publicId: string; request: UpdateContainerRequest; file: File | null }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ publicId, request, file }) => {
            if (file) {
              return store._imageService.uploadToCloudinary(file, Bucket.MENU_ICON).pipe(
                switchMap((iconUrl) => {
                  const requestWithImage: UpdateContainerRequest = {
                    ...request,
                    iconUrl: iconUrl
                  };
                  return store._containerService.update(publicId, requestWithImage).pipe(
                    tap((response) => {
                      if (response.success) {
                        patchState(store, { loading: false });
                        showMessage('Contenedor actualizado exitosamente', 'success');
                        loadContainers();
                      } else {
                        patchState(store, { loading: false });
                        handleHttpError(null, 'Error al actualizar el contenedor', response.message);
                      }
                    }),
                    catchError((err) => {
                      handleHttpError(err, 'Error al actualizar el contenedor');
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
              return store._containerService.update(publicId, request).pipe(
                tap((response) => {
                  if (response.success) {
                    patchState(store, { loading: false });
                    showMessage('Contenedor actualizado exitosamente', 'success');
                    loadContainers();
                  } else {
                    patchState(store, { loading: false });
                    handleHttpError(null, 'Error al actualizar el contenedor', response.message);
                  }
                }),
                catchError((err) => {
                  handleHttpError(err, 'Error al actualizar el contenedor');
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
            store._containerService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Contenedor eliminado exitosamente', 'success');
                  loadContainers();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar el contenedor', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar el contenedor');
                return of(null);
              })
            )
          )
        )
      ),

      getForUpdate: (publicId: string) => {
        return store._containerService.getForUpdate(publicId);
      },

      refresh: () => {
        loadContainers();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      init: () => {
        loadContainers();
      }
    };
  })
);

