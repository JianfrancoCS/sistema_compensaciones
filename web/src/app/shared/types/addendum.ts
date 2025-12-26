import { ApiResult, PagedResult } from './api';

export interface AddendumListDTO {
  publicId: string;
  addendumNumber: string;
  contractNumber: string;
  addendumTypeName: string;
  startDate: string;
  stateName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CommandAddendumResponse {
  id: string;
  addendumNumber: string;
  contractPublicId: string;
  addendumTypePublicId: string;
  statePublicId: string;
  templatePublicId: string;
  content: string;
  variables: AddendumVariableValuePayload[];
}

export interface AddendumVariableValuePayload {
  code: string;
  value: string;
}

export interface CreateAddendumRequest {
  contractPublicId: string;
  startDate: string;
  endDate?: string;
  templatePublicId: string;
  addendumTypePublicId: string;
  variables: AddendumVariableValuePayload[];
}

export interface UpdateAddendumRequest {
  addendumTypePublicId: string;
  statePublicId: string;
  templatePublicId: string;
  variables: AddendumVariableValuePayload[];
}

export interface AddendumTemplateListDTO {
  publicId: string;
  code: string;
  name: string;
  addendumTypeName: string;
  stateName: string;
  createdAt: string;
  updatedAt: string;
}

export interface AddendumTemplateVariableDTO {
  publicId: string;
  code: string;
  name: string;
}

export interface CommandAddendumTemplateResponse {
  id: string;
  code: string;
  name: string;
  templateContent: string;
  addendumTypePublicId: string;
  statePublicId: string;
  content: string;
  variables: AddendumTemplateVariableDTO[];
}

export interface AddendumTemplateVariableRequest {
  variablePublicId: string;
  isRequired: boolean;
  displayOrder: number;
}

export interface CreateAddendumTemplateRequest {
  name: string;
  templateContent: string;
  addendumTypePublicId: string;
  statePublicId: string;
  variables?: AddendumTemplateVariableRequest[];
}

export interface UpdateAddendumTemplateRequest extends CreateAddendumTemplateRequest {}

export interface CreateAddendumTypeRequest {
  name: string;
}

export interface UpdateAddendumTypeRequest {
  name: string;
}

export interface CommandAddendumTypeResponse {
  publicId: string;
  name: string;
}

export type ApiPagedAddendumsResponse = ApiResult<PagedResult<AddendumListDTO>>;
export type ApiCommandAddendumResponse = ApiResult<CommandAddendumResponse>;
export type ApiPagedAddendumTemplatesResponse = ApiResult<PagedResult<AddendumTemplateListDTO>>;
export type ApiCommandAddendumTemplateResponse = ApiResult<CommandAddendumTemplateResponse>;