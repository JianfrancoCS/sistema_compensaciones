import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { Profile, CreateProfileRequest, UpdateProfileRequest, ProfileParams, AssignElementsRequest, ProfileElementsByContainer } from '@shared/types/profile';
import { ProfileService } from '../services/profile.service';
import { MessageService } from 'primeng/api';

interface ProfileFilters {
  query: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface ProfileState {
  profiles: Profile[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  filters: ProfileFilters;
  elementsByContainer: ProfileElementsByContainer | null;
  loadingElements: boolean;
}

const initialState: ProfileState = {
  profiles: [],
  loading: false,
  error: null,
  totalElements: 0,
  filters: {
    query: '',
    page: 0,
    pageSize: 10,
    sortBy: 'name',
    sortDirection: 'ASC'
  },
  elementsByContainer: null,
  loadingElements: false
};

export const ProfileStore = signalStore(
  { providedIn: 'root' },
  withState<ProfileState>(initialState),

  withProps(() => ({
    _profileService: inject(ProfileService),
    _messageService: inject(MessageService)
  })),

  withComputed((store) => ({
    isEmpty: computed(() => !store.loading() && store.profiles().length === 0)
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

    const loadProfiles = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: ProfileParams = {
            query: filters.query,
            page: filters.page,
            pageSize: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._profileService.getProfiles(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  profiles: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar perfiles', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar perfiles');
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
        loadProfiles();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), query: '', page: 0 }
        });
        loadProfiles();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            query: '',
            page: 0,
            pageSize: 10,
            sortBy: 'name',
            sortDirection: 'ASC'
          }
        });
        loadProfiles();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadProfiles();
      },
      onLazyLoad: (event: any) => {
        const page = (event.first ?? 0) / (event.rows ?? 10);
        const pageSize = event.rows ?? 10;

        let sortBy = 'name';
        if (event.sortField) {
          if (Array.isArray(event.sortField)) {
            sortBy = event.sortField[0] || 'name';
          } else {
            sortBy = event.sortField;
          }
        }

        const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

        patchState(store, {
          filters: { ...store.filters(), page, pageSize, sortBy, sortDirection }
        });
        loadProfiles();
      },

      create: rxMethod<CreateProfileRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._profileService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Perfil creado exitosamente', 'success');
                  loadProfiles();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el perfil', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear el perfil');
                return of(null);
              })
            )
          )
        )
      ),

      update: rxMethod<{ publicId: string; request: UpdateProfileRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((params) =>
            store._profileService.update(params.publicId, params.request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Perfil actualizado exitosamente', 'success');
                  loadProfiles();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al actualizar el perfil', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar el perfil');
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
            store._profileService.delete(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Perfil eliminado exitosamente', 'success');
                  loadProfiles();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al eliminar el perfil', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al eliminar el perfil');
                return of(null);
              })
            )
          )
        )
      ),

      getForUpdate: (publicId: string) => {
        return store._profileService.getForUpdate(publicId);
      },

      loadElementsByContainer: rxMethod<string>(
        pipe(
          tap(() => patchState(store, { loadingElements: true, error: null })),
          switchMap((publicId) =>
            store._profileService.getElementsByContainer(publicId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, {
                    elementsByContainer: response.data,
                    loadingElements: false
                  });
                } else {
                  patchState(store, { loadingElements: false });
                  handleHttpError(null, 'Error al cargar elementos', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al cargar elementos');
                patchState(store, { loadingElements: false });
                return of(null);
              })
            )
          )
        )
      ),

      assignElements: rxMethod<{ publicId: string; request: AssignElementsRequest }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((params) =>
            store._profileService.assignElements(params.publicId, params.request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage('Elementos asignados exitosamente', 'success');
                  patchState(store, { loadingElements: true });
                  store._profileService.getElementsByContainer(params.publicId).subscribe({
                    next: (elementsResponse) => {
                      if (elementsResponse.success) {
                        patchState(store, {
                          elementsByContainer: elementsResponse.data,
                          loadingElements: false
                        });
                      } else {
                        patchState(store, { loadingElements: false });
                      }
                    },
                    error: () => {
                      patchState(store, { loadingElements: false });
                    }
                  });
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al asignar elementos', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al asignar elementos');
                return of(null);
              })
            )
          )
        )
      ),

      refresh: () => {
        loadProfiles();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      init: () => {
        loadProfiles();
      },

      loadAllForSelect: rxMethod<void>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(() => {
            const params: ProfileParams = {
              query: '',
              page: 0,
              pageSize: 1000, // Tamaño grande para obtener todos
              sortBy: 'name',
              sortDirection: 'ASC'
            };

            return store._profileService.getProfiles(params).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, {
                    profiles: response.data.data,
                    loading: false
                  });
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al cargar perfiles', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error de conexión al cargar perfiles');
                return of(null);
              })
            );
          })
        )
      )
    };
  })
);

