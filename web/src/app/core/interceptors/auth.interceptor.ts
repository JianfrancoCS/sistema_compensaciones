import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AuthStore } from '../store/auth.store';
import { catchError, switchMap, throwError, tap } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('cloudinary.com')) {
    return next(req);
  }

  if (req.url.includes('/v1/auth/login') || req.url.includes('/v1/auth/refresh')) {
    return next(req);
  }

  const authService = inject(AuthService);
  const router = inject(Router);
  const authStore = inject(AuthStore);
  const token = authService.getToken();

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/v1/auth/login') && !req.url.includes('/v1/auth/refresh')) {
        const refreshToken = authService.getRefreshToken();
        
        if (refreshToken) {
          return authService.refreshToken(refreshToken).pipe(
            tap(response => {
              if (response.success && response.data) {
                authStore.init();
              }
            }),
            switchMap(response => {
              if (response.success && response.data) {
                const newToken = response.data.token;
                const clonedReq = req.clone({
                  setHeaders: {
                    Authorization: `Bearer ${newToken}`
                  }
                });
                return next(clonedReq);
              } else {
                authService.clearAuth();
                authStore.logout();
                router.navigate(['/login']);
                return throwError(() => error);
              }
            }),
            catchError(refreshError => {
              authService.clearAuth();
              authStore.logout();
              router.navigate(['/login']);
              return throwError(() => refreshError);
            })
          );
        } else {
          authService.clearAuth();
          authStore.logout();
          router.navigate(['/login']);
        }
      }
      
      return throwError(() => error);
    })
  );
};
