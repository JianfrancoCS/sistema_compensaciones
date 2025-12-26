import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { Router } from '@angular/router';
import {
  UserDTO,
  CreateUserRequest,
  UpdateUserStatusRequest,
  SyncUserGroupsRequest,
  GroupAssignmentStatusDTO,
  UserParams,
  AssignProfileRequest,
  UserElementsByContainer,
  AssignUserElementsRequest,
  ProfileForAssignment,
  SyncUserProfilesRequest,
  UserDetailsDTO
} from '@shared/types/security';
import { UserService } from '../services/user.service';
import { AuthService } from '../services/auth.service';
import { MessageService } from 'primeng/api';

interface UserFilters {
  search: string;
  isActive?: boolean;
  positionId?: string; // UUID del cargo para filtrar
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface UserState {
  users: UserDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  filters: UserFilters;
  elementsByContainer: UserElementsByContainer | null;
  loadingElements: boolean;
  shouldRedirectToLogin: boolean;
  profilesForAssignment: ProfileForAssignment[] | null;
  loadingProfilesForAssignment: boolean;
  userDetails: UserDetailsDTO | null;
  loadingUserDetails: boolean;
}

const initialState: UserState = {
  users: [],
  loading: false,
  error: null,
  totalElements: 0,
  filters: {
    search: '',
    isActive: undefined,
    positionId: undefined,
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  },
  elementsByContainer: null,
  loadingElements: false,
  shouldRedirectToLogin: false,
  profilesForAssignment: null,
  loadingProfilesForAssignment: false,
  userDetails: null,
  loadingUserDetails: false
};

export const UserStore = signalStore(
  { providedIn: 'root' },
  withState<UserState>(initialState),

  withProps(() => ({
    _userService: inject(UserService),
    _authService: inject(AuthService),
    _router: inject(Router),
    _messageService: inject(MessageService)
  })),

  withComputed((store) => ({
    isEmpty: computed(() => !store.loading() && store.users().length === 0)
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
      const message = apiResponseMessage || err?.error?.message || err?.message || defaultMessage;
      showMessage(message, 'error');
      patchState(store, { loading: false, error: message });
    };

    const loadUsers = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: UserParams = {
            search: filters.search,
            isActive: filters.isActive,
            positionId: filters.positionId,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._userService.getUsers(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  users: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                  error: null
                });
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar usuarios');
              return of(null);
            })
          );
        })
      )
    );

    const createUser = rxMethod<CreateUserRequest>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((request) =>
          store._userService.createUser(request).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Usuario creado exitosamente', 'success');
                loadUsers();
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al crear usuario');
              return of(null);
            })
          )
        )
      )
    );

    const updateUserStatus = rxMethod<{ userId: string; isActive: boolean }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(({ userId, isActive }) =>
          store._userService.updateUserStatus(userId, { isActive }).pipe(
            tap((response) => {
              if (response.success) {
                showMessage(`Usuario ${isActive ? 'activado' : 'desactivado'} exitosamente`, 'success');
                loadUsers();
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al actualizar estado del usuario');
              return of(null);
            })
          )
        )
      )
    );

    const syncUserGroups = rxMethod<{ userId: string; groupIds: string[] }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(({ userId, groupIds }) =>
          store._userService.syncUserGroups(userId, { groupIds }).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Grupos actualizados exitosamente', 'success');
                loadUsers();
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al actualizar grupos');
              return of(null);
            })
          )
        )
      )
    );

    const assignProfile = rxMethod<{ userId: string; request: AssignProfileRequest }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(({ userId, request }) =>
          store._userService.assignProfile(userId, request).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Perfil asignado exitosamente', 'success');
                loadUsers();
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al asignar perfil');
              return of(null);
            })
          )
        )
      )
    );

    const loadElementsByContainer = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loadingElements: true, error: null })),
        switchMap((userId) =>
          store._userService.getElementsByContainer(userId).pipe(
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
    );

    const assignElements = rxMethod<{ userId: string; request: AssignUserElementsRequest }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null, shouldRedirectToLogin: false })),
        switchMap(({ userId, request }) =>
          store._userService.assignElements(userId, request).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Elementos asignados exitosamente. El usuario deberá volver a loguearse para ver los cambios.', 'success');
                
                const currentUserId = store._authService.getUserId();
                if (currentUserId === userId) {
                  patchState(store, { shouldRedirectToLogin: true });
                  store._authService.logout().subscribe(() => {
                    store._router.navigate(['/auth/login']);
                  });
                } else {
                  loadElementsByContainer(userId);
                }
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al asignar elementos');
              return of(null);
            })
          )
        )
      )
    );

    return {
      loadUsers,
      createUser,
      updateUserStatus,
      syncUserGroups,
      assignProfile,
      loadElementsByContainer,
      assignElements,
      setSearch: (search: string) => patchState(store, { filters: { ...store.filters(), search, page: 0 } }),
      setIsActiveFilter: (isActive?: boolean) => patchState(store, { filters: { ...store.filters(), isActive, page: 0 } }),
      setPositionFilter: (positionId?: string) => patchState(store, { filters: { ...store.filters(), positionId, page: 0 } }),
      setPage: (page: number) => patchState(store, { filters: { ...store.filters(), page } }),
      setPageSize: (pageSize: number) => patchState(store, { filters: { ...store.filters(), pageSize, page: 0 } }),
      loadPage: ({ page, pageSize, sortBy, sortDirection }: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, { filters: { ...store.filters(), page, pageSize, sortBy, sortDirection } });
        loadUsers();
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
          filters: { 
            ...store.filters(), 
            page, 
            pageSize, 
            sortBy, 
            sortDirection 
          } 
        });
        loadUsers();
      },
      loadProfilesForAssignment: rxMethod<string>(
        pipe(
          tap(() => patchState(store, { loadingProfilesForAssignment: true, error: null })),
          switchMap((userId) =>
            store._userService.getProfilesForAssignment(userId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, {
                    profilesForAssignment: response.data,
                    loadingProfilesForAssignment: false
                  });
                } else {
                  patchState(store, { loadingProfilesForAssignment: false });
                  handleHttpError(null, 'Error al cargar perfiles', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al cargar perfiles');
                patchState(store, { loadingProfilesForAssignment: false });
                return of(null);
              })
            )
          )
        )
      ),
      syncUserProfiles: rxMethod<{ userId: string; profileIds: string[] }>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap(({ userId, profileIds }) =>
            store._userService.syncUserProfiles(userId, { profileIds }).pipe(
              tap((response) => {
                if (response.success) {
                  showMessage('Perfiles actualizados exitosamente', 'success');
                  loadUsers();
                } else {
                  handleHttpError(null, response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al actualizar perfiles');
                return of(null);
              })
            )
          )
        )
      ),
      resetFilters: () => {
        patchState(store, {
          filters: {
            search: '',
            isActive: undefined,
            positionId: undefined,
            page: 0,
            pageSize: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC'
          }
        });
        loadUsers();
      },
      loadUserDetails: rxMethod<string>(
        pipe(
          tap(() => patchState(store, { loadingUserDetails: true, error: null })),
          switchMap((userId) =>
            store._userService.getUserDetails(userId).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, {
                    userDetails: response.data,
                    loadingUserDetails: false
                  });
                } else {
                  patchState(store, { loadingUserDetails: false });
                  handleHttpError(null, 'Error al cargar detalles del usuario', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al cargar detalles del usuario');
                patchState(store, { loadingUserDetails: false });
                return of(null);
              })
            )
          )
        )
      ),
      clearUserDetails: () => patchState(store, { userDetails: null }),
      init: () => loadUsers()
    };
  })
);
