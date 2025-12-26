import { ApiResult, PagedResult, SelectOption } from './api';

export interface ContractTemplateListDTO {
  publicId: string;
  code: string;
  name: string;
  contractTypeName: string;
  stateName: string;
  createdAt: string;
  updatedAt: string;
}

export interface ContractTemplateVariableDTO {
  publicId: string;
  code: string;
  name: string;
  dataType: string;
  defaultValue: string;
  isRequired: boolean;
  displayOrder: number;
  validationRegex: string;
  validationErrorMessage: string;
  hasValidation: boolean;
}

export interface CommandContractTemplateResponse {
  publicId: string;
  code: string;
  name: string;
  templateContent: string;
  contractTypePublicId: string;
  statePublicId: string;
  variables: ContractTemplateVariableDTO[];
  content?: string;
}

export interface ContractTemplateSelectOptionDTO extends SelectOption {}

export interface ContractTemplatePageableRequest {
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
  code?: string | null;
  name?: string | null;
  contractTypePublicId?: string | null;
  statePublicId?: string | null;
}

export interface CreateContractTemplateRequest {
  name: string;
  templateContent: string;
  contractTypePublicId: string;
  statePublicId: string;
  variables: { variablePublicId: string; isRequired: boolean; displayOrder: number }[];
}

export interface UpdateContractTemplateRequest {
  name: string;
  templateContent: string;
  contractTypePublicId: string;
  statePublicId: string;
  variables: { variablePublicId: string; isRequired: boolean; displayOrder: number }[];
}

export type ApiPagedContractTemplatesResponse = ApiResult<PagedResult<ContractTemplateListDTO>>;
export type ApiCommandContractTemplateResponse = ApiResult<CommandContractTemplateResponse>;
export type ApiContractTemplateSelectOptionsResponse = ApiResult<ContractTemplateSelectOptionDTO[]>;
