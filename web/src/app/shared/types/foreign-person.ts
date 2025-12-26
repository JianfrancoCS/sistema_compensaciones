import { BasePageableRequest, ApiResult } from './index';

export interface CreateForeignPersonRequest {
  documentNumber: string;
  names: string;
  paternalLastname: string;
  maternalLastname: string;
  dateOfBirth: string;
}

export interface UpdateForeignPersonRequest {
  names: string;
  paternalLastname: string;
  maternalLastname: string;
  dateOfBirth: string;
}

export interface ForeignPersonResponse {
  documentNumber: string;
  names: string;
  paternalLastname: string;
  maternalLastname: string;
  isNational: boolean;
}

export interface ForeignPersonPageableRequest extends BasePageableRequest {
  isNational?: boolean;
}

export type ApiCreateForeignPersonResponse = ApiResult<ForeignPersonResponse>;
export type ApiUpdateForeignPersonResponse = ApiResult<ForeignPersonResponse>;