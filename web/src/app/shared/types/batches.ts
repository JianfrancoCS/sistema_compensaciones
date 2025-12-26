import { HttpParams } from '@angular/common/http';
import { SelectOption, PagedRequest, ApiResult, PagedResult } from './api';

export interface BatchListDTO {
  publicId: string;
  name: string;
  hectareage: number;
  subsidiaryName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBatchRequest {
  name: string;
  hectareage: number;
  subsidiaryPublicId: string;
}

export interface UpdateBatchRequest {
  name: string;
  hectareage: number;
  subsidiaryPublicId: string;
}

export interface BatchDetailsDTO {
  publicId: string;
  name: string;
  hectareage: number;
  subsidiaryPublicId: string;
  createdAt: string;
  updatedAt: string;
}

export interface BatchSelectOptionDTO extends SelectOption {
  publicId: string;
  name: string;
}

export interface BatchPageableRequest extends PagedRequest {
  name?: string;
  subsidiaryPublicId?: string;
}

export interface BatchState {
  batches: BatchListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  filters: {
    name: string;
    subsidiaryPublicId: string;
    page: number;
    pageSize: number;
    sortBy: string;
    sortDirection: 'ASC' | 'DESC'; // Corregido el tipo aquí
  };
}

export class BatchParams {
  constructor(
    public name: string,
    public subsidiaryPublicId: string,
    public page: number,
    public size: number,
    public sortBy: string,
    public sortDirection: string
  ) {}

  toHttpParams(): HttpParams {
    let params = new HttpParams();
    if (this.name) {
      params = params.append('pageableRequest.name', this.name);
    }
    if (this.subsidiaryPublicId) {
      params = params.append('pageableRequest.subsidiaryPublicId', this.subsidiaryPublicId);
    }
    params = params.append('pageableRequest.page', this.page.toString());
    params = params.append('pageableRequest.size', this.size.toString());
    params = params.append('pageableRequest.sortBy', this.sortBy);
    params = params.append('pageableRequest.sortDirection', this.sortDirection);
    return params;
  }
}

export type ApiResultPagedBatchListDTO = ApiResult<PagedResult<BatchListDTO>>;
export type ApiResultCommandBatchResponse = ApiResult<BatchDetailsDTO>;
export type ApiResultBatchDetailsDTO = ApiResult<BatchDetailsDTO>;
export type ApiResultBatchSelectOptionDTOList = ApiResult<BatchSelectOptionDTO[]>;
export type ApiResultVoid = ApiResult<void>;
