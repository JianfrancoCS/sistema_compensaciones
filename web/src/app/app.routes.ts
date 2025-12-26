import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('@layouts/landing-layout/landing-layout-routes').then(m => m.landingLayoutRoutes),
  },
  {
    path: 'system',
    loadChildren: () =>
      import('@layouts/payroll-layout/payroll-routes').then(m => m.payrollRoutes),
  },
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./unauthorized/unauthorized').then(m => m.Unauthorized)
  }
];
