import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { Router } from '@angular/router';
import { ApiResult } from '@shared/types/api';
import { AuthService } from '../services/auth.service';
import { MessageService } from 'primeng/api';
import { LoginRequest, LoginResponse, NavigationItem } from '@core/models/auth.model';

interface AuthState {
  isAuthenticated: boolean;
  username: string | null;
  token: string | null;
  menu: NavigationItem[];
  loading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  isAuthenticated: false,
  username: null,
  token: null,
  menu: [],
  loading: false,
  error: null
};

export const AuthStore = signalStore(
  withState<AuthState>(initialState),

  withProps(() => ({
    _authService: inject(AuthService),
    _router: inject(Router),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    hasMenu: computed(() => state.menu().length > 0)
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
      patchState(store, { loading: false, error: errorForState });
    };

    const login = rxMethod<LoginRequest>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((request) =>
          store._authService.login(request.username, request.password).pipe(
            tap((response: ApiResult<LoginResponse>) => {
              if (response.success && response.data) {
                patchState(store, {
                  isAuthenticated: true,
                  username: response.data.username,
                  token: response.data.token,
                  menu: response.data.menu || [],
                  loading: false,
                  error: null
                });
                showMessage(response.message || 'Inicio de sesión exitoso', 'success');
                store._router.navigate(['/system']);
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al iniciar sesión', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al iniciar sesión');
              return of(null);
            })
          )
        )
      )
    );

    const logout = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true })),
        switchMap(() =>
          store._authService.logout().pipe(
            tap((response: ApiResult<void>) => {
              patchState(store, {
                isAuthenticated: false,
                username: null,
                token: null,
                menu: [],
                loading: false,
                error: null
              });
              store._router.navigate(['/home']);
            }),
            catchError((err) => {
              patchState(store, {
                isAuthenticated: false,
                username: null,
                token: null,
                menu: [],
                loading: false
              });
              store._router.navigate(['/home']);
              return of(null);
            })
          )
        )
      )
    );

    const init = () => {
      const token = store._authService.getToken();
      const username = store._authService.getUsername();
      const menu = store._authService.getMenu();

      if (token) {
        patchState(store, {
          isAuthenticated: true,
          username,
          token,
          menu
        });
      }
    };

    return {
      login,
      logout,
      init
    };
  })
);