export interface ExternalMarkRequest {
  personDocumentNumber: string;
  subsidiaryPublicId: string;
  markingReasonPublicId: string;
  isEntry: boolean;
}

export interface EmployeeMarkRequest {
  personDocumentNumber: string;
  subsidiaryPublicId: string;
  markingReasonPublicId: string;
  isEntry: boolean;
}

export interface MarkingResponse {
  publicId: string;
  personDocumentNumber: string;
  entryType: string;
  markingReasonName: string;
  subsidiaryName: string;
  markedAt: string;
}

import { ApiResult } from './api';

export type ApiMarkingResponse = ApiResult<MarkingResponse>;