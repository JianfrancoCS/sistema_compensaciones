import { ApiResult, PagedResult } from './api';
import {BasePageableRequest} from '@shared/types/base';



export interface ContractTypeListDTO {
  publicId: string;
  code: string;
  name: string;
  description: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ContractTypeDetailsDTO {
  publicId: string;
  code: string;
  name: string;
  description: string;
  isActive: boolean;
}

export interface ContractTypeSelectOptionDTO {
  publicId: string;
  name: string;
}

export interface CreateContractTypeRequest {
  code: string;
  name: string;
  description: string;
}

export interface UpdateContractTypeRequest {
  code: string;
  name: string;
  description: string;
  isActive: boolean;
}

export interface CommandContractTypeResponse {
  publicId: string;
  code: string;
  name: string;
  description: string;
  isActive: boolean;
}

export interface ContractTypePageableRequest extends BasePageableRequest {
  code?: string;
  name?: string;
  isActive?: boolean;
}

export type ApiPagedContractTypesResponse = ApiResult<PagedResult<ContractTypeListDTO>>;
export type ApiContractTypeDetailsResponse = ApiResult<ContractTypeDetailsDTO>;
export type ApiCommandContractTypeResponse = ApiResult<CommandContractTypeResponse>;
export type ApiContractTypeSelectOptionsResponse = ApiResult<ContractTypeSelectOptionDTO[]>;
