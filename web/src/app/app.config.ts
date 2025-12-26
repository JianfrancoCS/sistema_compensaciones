import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection, APP_INITIALIZER, inject } from '@angular/core';
import { provideRouter, withInMemoryScrolling, withRouterConfig } from '@angular/router';
import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { providePrimeNG } from 'primeng/config';
import Aura from "@primeuix/themes/aura";
import { provideHttpClient, withFetch, withInterceptors } from "@angular/common/http";
import { MessageService, ConfirmationService } from 'primeng/api';
import { SubsidiaryStore } from '@core/store/subsidiary.store';
import { AreaStore } from '@core/store/area.store';
import { PositionStore} from '@core/store/position.store';
import { EmployeeStore} from '@core/store/employee.store';
import { ContractTemplateStore } from '@core/store/contract-template.store';
import { BarcodeScannerStore } from '@core/store/barcode-scanner.store';
import { VariableStore } from '@core/store/variables.store';
import { LocationStore } from '@core/store/location.store';
import { GroupStore } from '@core/store/group.store';
import { JustificationStore } from '@core/store/justification.store';
import { PayrollConfigurationStore } from '@core/store/payroll-configuration.store';

import Quill from 'quill';
import {AttendanceStore} from '@core/store/attendance.store';
import {ContractStore} from '@core/store/contracts.store';
import {CreateContractStore} from '@core/store/create-contract.store';
import {PersonStore} from '@core/store/person.store';
import {LaborUnitStore} from '@core/store/labor-unit.store';
import {BatchStore} from '@core/store/batches.store';
import {CompanyStore} from '@core/store/company.store';
import {PeriodStore} from '@core/store/period.store';
import {UserStore} from '@core/store/user.store';
import {RoleStore} from '@core/store/role.store';
import {AuthStore} from '@core/store/auth.store';
import {AuthService} from '@core/services/auth.service';
import {authInterceptor} from '@core/interceptors/auth.interceptor';

const Font = Quill.import('formats/font') as any;
Font.whitelist = ['arial'];
Quill.register(Font, true);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(
      routes,
      withInMemoryScrolling({
        anchorScrolling: 'enabled',
        scrollPositionRestoration: 'enabled',
      }),
      withRouterConfig({
        onSameUrlNavigation: 'reload'
      })
    ),
    provideAnimationsAsync(),
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor])
    ),
    {
      provide: APP_INITIALIZER,
      useFactory: () => {
        const authStore = inject(AuthStore);
        return () => authStore.init();
      },
      multi: true,
      deps: []
    },
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: '.dark-mode',
          cssLayer: {
            name: 'primeng',
            order: 'theme, base, primeng'
          }
        },
      }
    }),
    MessageService,
    ConfirmationService,
    SubsidiaryStore,
    AreaStore,
    PositionStore,
    EmployeeStore,
    ContractTemplateStore,
    AttendanceStore,
    ContractStore,
    CreateContractStore,
    PersonStore,
    BarcodeScannerStore,
    VariableStore,
    LocationStore,
    GroupStore,
    BatchStore,
    CompanyStore,
    JustificationStore,
    LaborUnitStore,
    PeriodStore,
    PayrollConfigurationStore,
    UserStore,
    RoleStore,
    AuthStore
  ]
};
