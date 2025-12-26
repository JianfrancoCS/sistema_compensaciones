export default interface PermissionsState {
  permissions: string[];
  roles: string[];
  isLoading: boolean;
  userInfo: {
    sub: string;
    name: string;
    given_name: string;
    family_name: string;
    email: string;
    email_verified: boolean;
    preferred_username: string;
  };
  isAuthenticated: boolean;
}
