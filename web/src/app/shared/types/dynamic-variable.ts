import { ApiResult, PagedResult } from './api';
import { BasePageableRequest } from './base';

export interface DynamicVariableMethodRequest {
  methodPublicId: string;
  value?: string;
  executionOrder: number;
}

export interface CreateDynamicVariableRequest {
  name: string;
  errorMessage?: string;
  isActive: boolean;
  methods: DynamicVariableMethodRequest[];
}

export interface UpdateDynamicVariableRequest {
  name: string;
  errorMessage?: string;
  isActive: boolean;
  methods: DynamicVariableMethodRequest[];
}

export interface AppliedMethodDTO {
  methodName: string;
  value: string;
  order: number;
}

export interface CommandDynamicVariableResponse {
  publicId: string;
  code: string;
  name: string;
  finalRegex: string;
  errorMessage: string;
  isActive: boolean;
  methodsApplied: AppliedMethodDTO[];
}

export interface DynamicVariableListDTO {
  publicId: string;
  code: string;
  name: string;
  finalRegex: string;
  errorMessage: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DynamicVariablePageableRequest extends BasePageableRequest {
  name?: string;
  isActive?: boolean;
}

export type ApiPagedDynamicVariablesResponse = ApiResult<PagedResult<DynamicVariableListDTO>>;
export type ApiCommandDynamicVariableResponse = ApiResult<CommandDynamicVariableResponse>;