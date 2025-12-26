import PermissionsState from '../models/AuthState';
import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {adminMockData } from "../../shared/mocks/jwt/admin.mock"
import {computed} from '@angular/core';

const initialState: PermissionsState = adminMockData;

export const PermissionStore = signalStore(
  {providedIn: 'root'},
  withState(initialState),
  withComputed((state) => ({
    hasPermission: computed(() => (permission: string) =>
      state.permissions().includes(permission)
    ),
    hasRole: computed(() => (roleName: string) =>
      state.roles().some(role => role  === roleName)
    )
  })),
  withMethods((store) => (
    {
    setUserData: (userData: PermissionsState) => {
      patchState(store, userData);
    },
    logout: () => {
      patchState(store, {
        permissions: [],
        roles: [],
        isLoading: false,
        userInfo: {
          sub: '',
          name: '',
          given_name: '',
          family_name: '',
          email: '',
          email_verified: false,
          preferred_username: ''
        },
        isAuthenticated: false
      });
    }
  })))
