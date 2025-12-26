import { ApiResult, PagedResult } from './api';
import { BasePageableRequest } from '@shared/types/base';

export interface LaborListDTO {
  publicId: string;
  name: string;
  description: string;
  minTaskRequirement: number;
  laborUnitName: string;
  isPiecework: boolean;
  basePrice: number;
  createdAt: string;
  updatedAt: string;
}

export interface LaborDetailsDTO {
  publicId: string;
  name: string;
  description: string;
  minTaskRequirement: number;
  laborUnitPublicId: string;
  isPiecework: boolean;
  basePrice: number;
}

export interface CreateLaborRequest {
  name: string;
  description: string;
  minTaskRequirement: number;
  laborUnitPublicId: string;
  isPiecework: boolean;
  basePrice: number;
}

export interface UpdateLaborRequest {
  name: string;
  description: string;
  minTaskRequirement: number;
  laborUnitPublicId: string;
  isPiecework: boolean;
  basePrice: number;
}

export interface CommandLaborResponse {
  publicId: string;
  name: string;
  description: string;
  minTaskRequirement: number;
  laborUnitPublicId: string;
  isPiecework: boolean;
  basePrice: number;
}

export interface LaborSelectOptionDTO {
  publicId: string;
  name: string;
  isPiecework: boolean;
}

export interface LaborPageableRequest extends BasePageableRequest {
  name?: string;
  isPiecework?: boolean;
  laborUnitPublicId?: string;
}

export interface LaborFilters {
  name: string;
  isPiecework: boolean | undefined;
  laborUnitPublicId: string | undefined; // Changed from string | null to string | undefined
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}

export interface LaborState {
  labors: LaborListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: LaborSelectOptionDTO[];
  filters: LaborFilters;
}

export type ApiPagedLaborsResponse = ApiResult<PagedResult<LaborListDTO>>;
export type ApiLaborDetailsResponse = ApiResult<LaborDetailsDTO>;
export type ApiCommandLaborResponse = ApiResult<CommandLaborResponse>;
export type ApiLaborSelectOptionsResponse = ApiResult<LaborSelectOptionDTO[]>;
