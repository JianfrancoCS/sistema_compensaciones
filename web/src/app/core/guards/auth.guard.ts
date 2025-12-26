import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthStore } from '../store/auth.store';

export const canActivateAuthRole: CanActivateFn = async (route, state) => {
  const router = inject(Router);
  const authStore = inject(AuthStore);

  authStore.init();

  const authenticated = authStore.isAuthenticated();

  if (!authenticated) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  const requiredRoles = route.data['roles'] as string[];

  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  return true;
};