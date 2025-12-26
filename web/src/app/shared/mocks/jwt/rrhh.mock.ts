import PermissionsState from '../../../core/models/AuthState';
import {PERMISSIONS} from '../../../core/constants/permissions.constans';

export const adminMockData: PermissionsState = {
  permissions: [
    PERMISSIONS.MANAGER_PEOPLE,
    PERMISSIONS.MANAGER_TASKS,
    PERMISSIONS.MANAGER_ATTENDANCE,
    PERMISSIONS.MANAGER_PAYROLLS,
    PERMISSIONS.MANAGER_USERS
  ],
  roles: ['rrhh'],
  isLoading: false,
  userInfo: {
    sub: '2222-2222-2222-2222',
    name: 'María Supervisor',
    given_name: 'María',
    family_name: 'Supervisor',
    email: 'supervisor@empresa.com',
    email_verified: true,
    preferred_username: 'msupervisor'
  },
  isAuthenticated: true
}
