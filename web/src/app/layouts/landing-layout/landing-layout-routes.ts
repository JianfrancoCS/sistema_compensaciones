import { Routes } from '@angular/router';
import { LandingLayout } from './landing-layout';

export const landingLayoutRoutes: Routes = [
  {
    path: '',
    component: LandingLayout,
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('../../pages/login/login').then(m => m.Login)
      },
      {
        path: 'home',
        loadComponent: () =>
          import('../../pages/home/home').then(m => m.Home)
      },
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
      }
    ]
  }
];
