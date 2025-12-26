import { ApiResult, PagedResult } from './api';
import {BasePageableRequest} from '@shared/types/base';

export interface LaborUnitListDTO {
  publicId: string;
  name: string;
  abbreviation: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface LaborUnitDetailsDTO {
  publicId: string;
  name: string;
  abbreviation: string;
  description: string;
}

export interface CreateLaborUnitRequest {
  name: string;
  abbreviation: string;
  description: string;
}

export interface UpdateLaborUnitRequest {
  name: string;
  abbreviation: string;
  description: string;
}

export interface CommandLaborUnitResponse {
  publicId: string;
  name: string;
  abbreviation: string;
  description: string;
}

export interface LaborUnitSelectOptionDTO {
  publicId: string;
  name: string;
}

export interface LaborUnitPageableRequest extends BasePageableRequest {
  name?: string;
}

export interface LaborUnitParams {
  name?: string;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}

export interface LaborUnitFilters {
  name: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export interface LaborUnitState {
  laborUnits: LaborUnitListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: LaborUnitSelectOptionDTO[];
  filters: LaborUnitFilters;
}

export type ApiPagedLaborUnitsResponse = ApiResult<PagedResult<LaborUnitListDTO>>;
export type ApiLaborUnitDetailsResponse = ApiResult<LaborUnitDetailsDTO>;
export type ApiCommandLaborUnitResponse = ApiResult<CommandLaborUnitResponse>;
export type ApiLaborUnitSelectOptionsResponse = ApiResult<LaborUnitSelectOptionDTO[]>;
