import {BasePageableRequest} from '@shared/types/base';

export interface SubsidiaryListDTO {
  publicId: string;
  name: string;
  districtName: string;
  createdAt: string;
  updatedAt: string;
}

export interface SubsidiaryDetailsDTO {
  publicId: string;
  name: string;
  districtId: string;
  createdAt: string;
  updatedAt: string;
  employeeCount: number;
}

export interface CreateSubsidiaryRequest {
  name: string;
  districtPublicId: string;
}

export interface UpdateSubsidiaryRequest {
  name: string;
  districtPublicId: string;
}

export interface SubsidiaryParams extends BasePageableRequest{
  name?: string;
}
