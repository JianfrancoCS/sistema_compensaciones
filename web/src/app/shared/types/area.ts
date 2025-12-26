import { ApiResult, PagedResult } from './api';
import {BasePageableRequest} from '@shared/types/base';

export interface AreaListDTO {
  publicId: string;
  name: string;
  subsidiaryName: string;
  createdAt: string;
  updatedAt: string;
}

export interface AreaDetailsDTO {
  publicId: string;
  name: string;
  subsidiaryPublicId: string;
  subsidiaryName: string;
}

export interface CreateAreaRequest {
  name: string;
  subsidiaryPublicId?: string;
}

export interface UpdateAreaRequest {
  name: string;
}

export interface CommandAreaResponse {
  publicId: string;
  name: string;
  subsidiaryPublicId: string;
}

export interface AreaSelectOptionDTO {
  publicId: string;
  name: string;
}

export interface AreaPageableRequest extends BasePageableRequest {
  name?: string;
  subsidiaryPublicId?: string;
}

export interface AreaParams {
  name?: string;
  subsidiaryPublicId?: string;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}

export interface AreaFilters {
  name: string;
  subsidiaryPublicId: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export interface AreaState {
  areas: AreaListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: AreaSelectOptionDTO[];
  filters: AreaFilters;
}

export type ApiPagedAreasResponse = ApiResult<PagedResult<AreaListDTO>>;
export type ApiAreaDetailsResponse = ApiResult<AreaDetailsDTO>;
export type ApiCommandAreaResponse = ApiResult<CommandAreaResponse>;
export type ApiAreaSelectOptionsResponse = ApiResult<AreaSelectOptionDTO[]>;
