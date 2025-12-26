import {Routes} from '@angular/router';
import {PayrollLayout} from './payroll-layout';
import {Subsidiaries} from '@pages/subsidiaries/subsidiaries';
import { SubsidiarySignersComponent } from '@pages/subsidiary-signers/subsidiary-signers';
import {Areas} from '@pages/areas/areas';
import {Positions} from '@pages/positions/positions';
import {Concepts} from '@pages/concepts/concepts';
import {Employees} from '@pages/employees/employees';
import {ForeignPersonsComponent} from '@pages/foreign-persons/foreign-persons';
import {ContractTemplates} from '@pages/contract-templates/contract-templates';
import {CreateContractComponent} from '@pages/contract-templates/create-template-contract/create-template-contract';
import {Contracts} from '@pages/contracts/contracts';
import {CreateContract} from '@pages/contracts/create-contract/create-contract';
import {Addendums} from '@pages/addendums/addendums';
import {AddendumTemplates} from '@pages/addendum-templates/addendum-templates';
import {Variables} from '@pages/variables/variables';
import {AttendanceEntryComponent} from '@pages/attendance/entry/entry';
import {AttendanceExitComponent} from '@pages/attendance/exit/exit';
import {Company} from '@pages/company/company';
import {LaborsComponent} from '@pages/labors/labors';
import {BatchesComponent} from '@pages/batches/batches';
import {LaborUnits} from '@pages/labor-units/labor-units';
import {QrComponent} from '@pages/qr/qr';
import {JustificationsComponent} from '@pages/justifications/justifications';
import { QrDetailComponent } from '@pages/qr/detail/qr-detail';
import { CalendarComponent} from '@pages/calendar/calendar';
import { Periods } from '@pages/periods/periods';
import { PayrollsComponent } from '@pages/payrolls/payrolls.component';
import { PayrollSummaryComponent } from '@pages/payrolls/components/payroll-summary/payroll-summary.component';
import { PayrollEmployeeDetailComponent } from '@pages/payrolls/components/payroll-employee-detail/payroll-employee-detail.component';
import { TareosComponent } from '@pages/tareos/tareos.component';
import { TareoDetailComponent } from '@pages/tareos/tareo-detail/tareo-detail.component';
import { DashboardComponent } from '@pages/dashboard/dashboard.component';
import { PayslipsComponent } from '@pages/payslips/payslips.component';
import { PayslipViewerComponent } from '@pages/payslips/payslip-viewer/payslip-viewer.component';
import { PayrollConfigurationDetailComponent } from '@pages/payroll-configurations/payroll-configuration-detail.component';
import { CreatePayrollConfigurationComponent } from '@pages/payroll-configurations/create-payroll-configuration/create-payroll-configuration.component';
import { Users } from '@pages/users/users';
import { Containers } from '@pages/containers/containers';
import { Elements } from '@pages/elements/elements';
import { Profiles } from '@pages/profiles/profiles';
import { AssignElementsComponent } from '@pages/profiles/assign-elements/assign-elements';
import { AssignUserElementsComponent } from '@pages/users/assign-elements/assign-elements';
import { AssignProfilesComponent } from '@pages/users/assign-profiles/assign-profiles';
import { MyProfileComponent } from '@pages/my-profile/my-profile.component';
import { ChangePasswordComponent } from '@pages/my-profile/change-password/change-password.component';
import { FotocheckComponent } from '@pages/fotocheck/fotocheck';
import { canActivateAuthRole } from '@core/guards/auth.guard';

export const payrollRoutes: Routes = [
  {
    path: '',
    component: PayrollLayout,
    canActivate: [canActivateAuthRole],
    children: [
      {
        path: '',
        redirectTo: 'my-profile',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        component: DashboardComponent
      },
      {
        path: 'subsidiaries',
        component: Subsidiaries
      },
      {
        path: 'subsidiary-signers',
        component: SubsidiarySignersComponent
      },
      {
        path: 'areas',
        component: Areas
      },
      {
        path: 'positions',
        component: Positions
      },
      {
        path: 'settings/concepts',
        component: Concepts
      },
      {
        path: 'employees',
        component: Employees
      },
      {
        path: 'foreign-persons',
        component: ForeignPersonsComponent
      },
      {
        path: 'contracts',
        component: Contracts
      },
      {
        path: 'contracts/create',
        component: CreateContract
      },
      {
        path: 'contracts/edit/:id',
        component: CreateContract
      },
      {
        path: 'contract-templates',
        component: ContractTemplates
      },
      {
        path: 'contract-templates/create',
        component: CreateContractComponent
      },
      {
        path: 'contract-templates/edit/:id',
        component: CreateContractComponent
      },
      {
        path: 'addendums',
        component: Addendums
      },
      {
        path: 'addendum-templates',
        component: AddendumTemplates
      },
      {
        path: 'variables',
        component: Variables
      },
      {
        path: 'attendance',
        redirectTo: 'attendance/entry',
        pathMatch: 'full'
      },
      {
        path: 'attendance/entry',
        component: AttendanceEntryComponent
      },
      {
        path: 'attendance/exit',
        component: AttendanceExitComponent
      },
      {
        path: 'company',
        component: Company
      },
      {
        path: 'labors',
        component: LaborsComponent
      },
      {
        path: 'batches',
        component: BatchesComponent
      },
      {
        path: 'labor-units',
        component: LaborUnits
      },
      {
        path: 'qr',
        component: QrComponent
      },
      {
        path: 'qr/:id',
        component: QrDetailComponent
      },
      {
        path: 'justifications',
        component: JustificationsComponent
      },
      {
        path: 'calendar',
        component:CalendarComponent
      },
      {
        path: 'periods',
        component: Periods
      },
      {
        path: 'payrolls/:publicId/summary',
        component: PayrollSummaryComponent
      },
      {
        path: 'payrolls/:payrollPublicId/summary/:employeePublicId',
        component: PayrollEmployeeDetailComponent
      },
      {
        path: 'payrolls',
        component: PayrollsComponent
      },
      {
        path: 'tareos/:publicId',
        component: TareoDetailComponent
      },
      {
        path: 'tareos',
        component: TareosComponent
      },
      {
        path: 'payslips/:id/view',
        component: PayslipViewerComponent
      },
      {
        path: 'payslips',
        component: PayslipsComponent
      },
      {
        path: 'payroll-configurations/detail',
        component: PayrollConfigurationDetailComponent
      },
      {
        path: 'payroll-configurations/create',
        component: CreatePayrollConfigurationComponent
      },
      {
        path: 'payroll-configurations/edit',
        component: CreatePayrollConfigurationComponent
      },
      {
        path: 'settings/users',
        component: Users
      },
      {
        path: 'settings/users/:publicId/assign-elements',
        component: AssignUserElementsComponent
      },
      {
        path: 'settings/users/:publicId/assign-profiles',
        component: AssignProfilesComponent
      },
      {
        path: 'settings/containers',
        component: Containers
      },
      {
        path: 'settings/elements',
        component: Elements
      },
      {
        path: 'settings/profiles',
        component: Profiles
      },
      {
        path: 'settings/profiles/:publicId/assign-elements',
        component: AssignElementsComponent
      },
      {
        path: 'my-profile',
        component: MyProfileComponent
      },
      {
        path: 'my-profile/change-password',
        component: ChangePasswordComponent
      },
      {
        path: 'fotocheck',
        component: FotocheckComponent
      }
    ]
  }
];
