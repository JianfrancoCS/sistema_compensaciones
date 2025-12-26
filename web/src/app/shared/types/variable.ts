import { ApiResult, PagedResult } from './api';
import {BasePageableRequest} from '@shared/types/base';

export interface VariableSelectOption {
  publicId: string;
  code: string;
  name: string;
  defaultValue: string;
  isRequired: boolean;
}

export interface VariableListDTO {
  publicId: string;
  code: string;
  name: string;
  defaultValue: string;
  hasValidation?: boolean;
  validationCount?: number;
  validationMethodsCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CommandVariableResponse {
  publicId: string;
  code: string;
  name: string;
  defaultValue: string;
}

export interface CreateVariableRequest {
  code: string;
  name: string;
  defaultValue?: string;
}

export interface UpdateVariableRequest {
  code: string;
  name: string;
  defaultValue?: string;
}

export interface CreateVariableWithValidationRequest {
  code: string;
  name: string;
  defaultValue?: string;
  validation?: {
    methods: VariableMethodRequest[];
  } | null;
}

export interface UpdateVariableWithValidationRequest {
  code: string;
  name: string;
  defaultValue?: string;
  validation?: {
    methods: VariableMethodRequest[];
  } | null;
}

export interface VariablePageableRequest extends BasePageableRequest {
  code?: string;
  name?: string;
}

export interface VariableParams {
  code?: string;
  name?: string;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}

export interface VariableFilters {
  code: string;
  name: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export interface VariableState {
  variables: VariableListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: VariableSelectOption[];
  filters: VariableFilters;
  validationMethods: ValidationMethodDTO[];
  currentVariableValidation: VariableValidationDTO | null;
  validationLoading: boolean;
  validationError: string | null;
  searchLoading: boolean;
}

export interface ValidationMethodDTO {
  publicId: string;
  name: string;
  code: string;
  methodType: string;
  requiresValue: boolean;
  description?: string;
}

export interface VariableMethodRequest {
  methodPublicId: string;
  value: string | null;
  executionOrder: number;
}

export interface VariableValidationRequest {
  errorMessage: string;
  methods: VariableMethodRequest[];
}

export interface VariableMethodDTO {
  methodPublicId: string;
  methodName: string;
  methodDescription: string;
  requiresValue: boolean;
  methodType: string;
  value: string | null;
  executionOrder: number;
}

export interface VariableValidationDTO {
  publicId: string;
  code: string;
  name: string;
  defaultValue: string;
  finalRegex: string;
  errorMessage: string;
  methods: VariableMethodDTO[];
}

export interface VariableSelectOptionsParams {
  name?: string;
}

export interface ContractVariableWithValidation {
  publicId: string;
  code: string;
  name: string;
  dataType: string;
  defaultValue: string | null;
  isRequired: boolean;
  displayOrder: number;
  validation: {
    hasValidation: boolean;
    finalRegex: string;
    errorMessage: string | null;
    appliedMethods: AppliedValidationMethod[];
  };
}

export interface AppliedValidationMethod {
  methodPublicId: string;
  methodName: string;
  methodDescription: string;
  requiresValue: boolean;
  methodType: string;
  value: string | null;
  executionOrder: number;
}

export type ApiPagedVariablesResponse = ApiResult<PagedResult<VariableListDTO>>;
export type ApiCommandVariableResponse = ApiResult<CommandVariableResponse>;
export type ApiValidationMethodsResponse = ApiResult<ValidationMethodDTO[]>;
export type ApiVariableValidationResponse = ApiResult<VariableValidationDTO>;
export type ApiContractVariablesWithValidationResponse = ApiResult<ContractVariableWithValidation[]>;
