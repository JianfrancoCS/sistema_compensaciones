export interface EmployeeListDTO {
  publicId: string;
  documentNumber: string;
  names: string;
  paternalLastname: string;
  maternalLastname: string;
  subsidiaryName: string;
  positionName: string;
  isNational: boolean;
  createdAt: string;
  updatedAt: string;
  photoUrl?: string | null;
}

export interface EmployeeDetailsDTO {
  publicId: string;
  documentNumber: string;
  names: string;
  paternalLastname: string;
  maternalLastname: string;
  dob: string;
  subsidiaryName: string;
  positionName: string;
  areaName: string;
  manager?: {
    code: string;
    fullName: string;
  };
  photoUrl?: string | null;
}

export interface CreateEmployeeRequest {
  documentNumber: string;
  subsidiaryPublicId: string;
  positionPublicId: string;
}

export interface UpdateEmployeeRequest {
  districtPublicId?: string;
  subsidiaryPublicId?: string;
  positionPublicId?: string;
  statePublicId?: string;
  managerCode?: string;
}

export interface CommandEmployeeResponse {
  publicId: string;
  documentNumber: string;
  names: string;
  paternalLastname: string;
  maternalLastname: string;
  subsidiaryPublicId: string;
  positionPublicId: string;
  managerPublicId?: string;
}

export interface EmployeeParams {
  documentNumber?: string;
  personName?: string;
  subsidiaryPublicId?: string;
  positionPublicId?: string;
  isNational?: boolean;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}
