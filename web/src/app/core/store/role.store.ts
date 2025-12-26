import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import {
  GroupDTO,
  GroupSelectOptionDTO,
  CreateGroupRequest,
  UpdateGroupRolesRequest,
  PermissionAssignmentDTO,
  GroupParams
} from '@shared/types/security';
import { GroupService } from '../services/group.service';
import { PermissionService } from '../services/permission.service';
import { MessageService } from 'primeng/api';
import { SelectOption } from '@shared/types/api';

interface RoleFilters {
  search: string;
  page: number;
  pageSize: number;
}

interface RoleState {
  roles: GroupDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: SelectOption[];
  filters: RoleFilters;
}

const initialState: RoleState = {
  roles: [],
  loading: false,
  error: null,
  totalElements: 0,
  selectOptions: [],
  filters: {
    search: '',
    page: 0,
    pageSize: 10
  }
};

export const RoleStore = signalStore(
  { providedIn: 'root' },
  withState<RoleState>(initialState),

  withComputed((store) => ({
    isEmpty: computed(() => !store.loading() && store.roles().length === 0)
  })),

  withMethods((store, groupService = inject(GroupService), permissionService = inject(PermissionService), messageService = inject(MessageService)) => {
    const showMessage = (message: string, severity: 'success' | 'error') => {
      messageService.add({
        severity,
        summary: severity === 'success' ? '�xito' : 'Error',
        detail: message
      });
    };

    const handleHttpError = (err: any, defaultMessage: string) => {
      const message = err?.error?.message || err?.message || defaultMessage;
      showMessage(message, 'error');
      patchState(store, { loading: false, error: message });
    };

    const loadRoles = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: GroupParams = {
            search: filters.search,
            page: filters.page,
            size: filters.pageSize
          };

          return groupService.getGroups(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  roles: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                  error: null
                });
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar roles');
              return of(null);
            })
          );
        })
      )
    );

    const loadSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          groupService.getSelectOptions().pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { selectOptions: response.data });
              }
            }),
            catchError(() => of(null))
          )
        )
      )
    );

    const createRole = rxMethod<CreateGroupRequest>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((request) =>
          groupService.createGroup(request).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Rol creado exitosamente', 'success');
                loadRoles();
                loadSelectOptions();
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al crear rol');
              return of(null);
            })
          )
        )
      )
    );

    const syncRolePermissions = rxMethod<{ groupId: string; roleIds?: string[]; permissionIds?: string[] }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(({ groupId, roleIds, permissionIds }) =>
          groupService.syncGroupRoles(groupId, { roleIds: roleIds || [], permissionIds }).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Permisos actualizados exitosamente', 'success');
                loadRoles();
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al actualizar permisos');
              return of(null);
            })
          )
        )
      )
    );

    const deleteRole = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((groupId) =>
          groupService.deleteGroup(groupId).pipe(
            tap((response) => {
              if (response.success) {
                showMessage('Rol eliminado exitosamente', 'success');
                loadRoles();
                loadSelectOptions();
              } else {
                handleHttpError(null, response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al eliminar rol');
              return of(null);
            })
          )
        )
      )
    );

    return {
      loadRoles,
      loadSelectOptions,
      createRole,
      syncRolePermissions,
      deleteRole,
      setSearch: (search: string) => patchState(store, { filters: { ...store.filters(), search, page: 0 } }),
      setPage: (page: number) => patchState(store, { filters: { ...store.filters(), page } }),
      setPageSize: (pageSize: number) => patchState(store, { filters: { ...store.filters(), pageSize, page: 0 } }),
      resetFilters: () => {
        patchState(store, {
          filters: {
            search: '',
            page: 0,
            pageSize: 10
          }
        });
        loadRoles();
      },
      init: () => {
        loadRoles();
        loadSelectOptions();
      }
    };
  })
);