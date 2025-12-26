import { ApiResult, PagedResult } from './api';
import { BasePageableRequest } from '@shared/types/base';

export interface ConceptListDTO {
  publicId: string;
  code: string;
  name: string;
  description: string | null;
  categoryName: string;
  value: number | null;
  calculationPriority: number;
  createdAt: string;
  updatedAt: string;
}

export interface ConceptDetailsDTO {
  publicId: string;
  code: string;
  name: string;
  description: string | null;
  categoryPublicId: string;
  categoryName: string;
  value: number | null;
  calculationPriority: number;
}

export interface CreateConceptRequest {
  code: string;
  name: string;
  description?: string;
  categoryPublicId: string;
  value?: number;
  calculationPriority: number;
}

export interface UpdateConceptRequest {
  code: string;
  name: string;
  description?: string;
  categoryPublicId: string;
  value?: number;
  calculationPriority: number;
}

export interface CommandConceptResponse {
  publicId: string;
  code: string;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConceptSelectOptionDTO {
  publicId: string;
  name: string;
  value: number | null;
  categoryName: string;
}

export interface ConceptPageableRequest extends BasePageableRequest {
  name?: string;
}

export interface ConceptParams {
  name?: string;
  categoryPublicId?: string;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}

export interface ConceptFilters {
  name: string;
  categoryPublicId: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export interface ConceptState {
  concepts: ConceptListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: ConceptSelectOptionDTO[];
  filters: ConceptFilters;
  categories: Array<{publicId: string, code: string, name: string}>;
  loadingCategories: boolean;
  conceptsByCategory: Record<string, ConceptSelectOptionDTO[]>;
  loadingConceptsByCategory: Record<string, boolean>;
}

export type ConceptListApiResult = ApiResult<PagedResult<ConceptListDTO>>;
export type ConceptDetailsApiResult = ApiResult<ConceptDetailsDTO>;
export type CommandConceptApiResult = ApiResult<CommandConceptResponse>;
export type ConceptSelectOptionListApiResult = ApiResult<ConceptSelectOptionDTO[]>;
