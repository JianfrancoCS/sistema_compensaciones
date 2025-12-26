import { BasePageableRequest, ApiResult } from './index';

export interface DocumentTypeSelectOptionDTO {
  publicId: string;
  code: string;
  name: string;
  length: number;
}

export interface DocumentTypeParams extends BasePageableRequest {
}

export type ApiDocumentTypesResponse = ApiResult<DocumentTypeSelectOptionDTO[]>;