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
  roles: ['admin'],
  isLoading: false,
  userInfo: {
    sub: '1111-1111-1111-1111',
    name: 'Carlos Admin',
    given_name: 'Carlos',
    family_name: 'Admin',
    email: 'admin@empresa.com',
    email_verified: true,
    preferred_username: 'cadmin'
  },
  isAuthenticated: true
}
