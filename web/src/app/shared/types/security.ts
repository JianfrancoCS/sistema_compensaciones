export interface UserDTO {
  publicId: string;
  username: string;
  employeeId?: string | null; // NULL para usuarios admin que no son empleados
  positionId?: string | null; // UUID del cargo (position)
  positionName?: string | null; // Nombre del cargo
  isActive: boolean;
  lastLoginAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  positionId?: string | null;
  employeeId?: string | null;
}

export interface UpdateUserStatusRequest {
  isActive: boolean;
}

export interface AssignProfileRequest {
  profileId: string;
}

export interface ProfileForAssignment {
  publicId: string;
  name: string;
  description?: string | null;
  isSelected: boolean;
  username?: string;
}

export interface SyncUserProfilesRequest {
  profileIds: string[];
}

export interface SyncUserGroupsRequest {
  groupIds: string[];
}

export interface UserParams {
  search?: string;
  isActive?: boolean;
  positionId?: string; // UUID del cargo para filtrar
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: string;
}

export interface GroupDTO {
  id: string;
  name: string;
}

export interface GroupSelectOptionDTO {
  publicId: string;
  name: string;
}

export interface CreateGroupRequest {
  name: string;
  permissions?: string[];
  permissionIds?: string[];
}

export interface UpdateGroupRolesRequest {
  roleIds: string[];
  permissionIds?: string[];
}

export interface GroupParams {
  search?: string;
  page: number;
  size: number;
}

export interface RoleDTO {
  id: string;
  name: string;
}

export interface PermissionAssignmentDTO {
  id: string;
  name: string;
  assigned: boolean;
}

export interface PermissionAssignmentStatusDTO {
  id: string;
  name: string;
  assigned: boolean;
}

export interface AvailableRoleDTO {
  id: string;
  name: string;
}

export interface GroupAssignmentStatusDTO {
  id: string;
  name: string;
  assigned: boolean;
}

export interface UserElementsByContainer {
  userPublicId: string;
  username: string;
  containers: UserContainerWithElements[];
}

export interface UserContainerWithElements {
  containerPublicId: string | null;
  containerName: string;
  containerDisplayName: string;
  containerIcon: string | null;
  elements: UserElementInfo[];
  selectedElementPublicIds: string[];
}

export interface UserElementInfo {
  publicId: string;
  name: string;
  displayName: string;
  route: string | null;
  icon: string | null;
}

export interface AssignUserElementsRequest {
  elementPublicIds: string[];
}

export interface UserDetailsDTO {
  userPublicId: string;
  username: string;
  isActive: boolean;
  employee: UserEmployeeInfo | null;
  contract: UserContractInfo | null;
}

export interface UserEmployeeInfo {
  employeeCode: string; // UUID serializado como string
  documentNumber: string;
  names: string | null;
  paternalLastname: string | null;
  maternalLastname: string | null;
  dateOfBirth: string | null; // LocalDate serializado como string (ISO format)
  gender: string | null;
  positionId: string | null; // UUID serializado como string
  positionName: string | null;
  positionSalary: number | null;
  customSalary: number | null;
  dailyBasicSalary: number | null;
  hireDate: string | null; // LocalDate serializado como string (ISO format)
  subsidiaryName: string | null;
  stateName: string | null;
  afpAffiliationNumber: string | null;
  bankAccountNumber: string | null;
  bankName: string | null;
}

export interface UserContractInfo {
  contractPublicId: string;
  contractNumber: string;
  startDate: string;
  endDate: string | null;
  extendedEndDate: string | null;
  contractTypeName: string | null;
  stateName: string | null;
}