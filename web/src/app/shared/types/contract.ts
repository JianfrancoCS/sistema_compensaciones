export interface ContractListDTO {
  publicId: string;
  contractNumber: string;
  personDocumentNumber: string;
  contractTypeName: string;
  startDate: string;
  endDate: string;
  stateName: string;
  isSigned: boolean;
  hasImages: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CommandContractResponse {
  publicId: string; // UUID del contrato (no el ID numérico por seguridad)
  contractNumber: string;
  personDocumentNumber: string;
  startDate: string;
  endDate?: string | null;
  contractTypePublicId: string;
  state: {
    publicId: string;
  };
  subsidiary: {
    publicId: string;
  };
  position: {
    publicId: string;
    areaPublicId: string;
  };
  template: {
    publicId: string;
    contractTypePublicId: string;
  };
  variables: ContractVariableValuePayload[];
}

export interface FileWithOrder {
  file: File;
  order: number;
  uploading: boolean;
  uploaded: boolean;
  cloudinaryUrl?: string;
  error?: string;
}

export interface ContractVariableValuePayload {
  code: string;
  value: string;
}

export interface CreateContractRequest {
  personDocumentNumber: string;
  names: string;
  paternalSurname: string;
  maternalSurname: string;
  dateOfBirth: string;
  startDate: string;
  endDate?: string | null;
  subsidiaryPublicId: string;
  positionPublicId: string;
  contractTypePublicId: string;
  templatePublicId: string;
  variables: ContractVariableValuePayload[];
  retirementConceptPublicId?: string | null;
  healthInsuranceConceptPublicId?: string | null;
}

export interface UpdateContractRequest {
  contractTypePublicId: string;
  statePublicId: string;
  subsidiaryPublicId: string;
  positionPublicId: string;
  templatePublicId: string;
  startDate: string;
  endDate?: string | null;
  variables: ContractVariableValuePayload[];
}

export interface ContractImageDTO {
  publicId: string;
  url: string;
  order: number;
}

export interface ContractDetailsDTO {
  publicId: string;
  contractNumber: string;
  startDate: string;
  endDate: string;
  content: string;
  variables: string;
  personDocumentNumber: string;
  personFullName: string;
  contractTypePublicId: string;
  contractTypeName: string;
  statePublicId: string;
  stateName: string;
  imageUrls: ContractImageDTO[];
}

export interface GenerateUploadUrlRequest {
  fileName: string;
}

export interface UploadUrlResponse {
  uploadUrl: string;
  apiKey: string;
  timestamp: number;
  signature: string;
  folder: string;
}

export interface AttachFileRequest {
  imagesUri: string[];
}

export interface SignContractRequest {
  statePublicId: string;
}

export interface CancelContractRequest {
  statePublicId: string;
}

export interface ContractSearchDTO {
  publicId: string;
  contractNumber: string;
  startDate: string;
  endDate: string;
  personDocumentNumber: string;
  personFullName: string;
  contractTypeName: string;
  stateName: string;
  imageUrls: ContractImageDTO[];
}

export interface ContractParams {
  contractNumber?: string;
  contractTypePublicId?: string;
  statePublicId?: string;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}

import { ApiResult, PagedResult } from './api';

export type ApiPagedContractsResponse = ApiResult<PagedResult<ContractListDTO>>;
export type ApiCommandContractResponse = ApiResult<CommandContractResponse>;
export type ApiContractDetailsResponse = ApiResult<ContractDetailsDTO>;
export type ApiUploadUrlResponse = ApiResult<UploadUrlResponse>;
export type ApiContractSearchResponse = ApiResult<ContractSearchDTO>;