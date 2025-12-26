import { ApiResult } from './api';

export interface PayrollConfigurationConceptAssignmentDTO {
  conceptPublicId: string;
  name: string;
  isAssigned: boolean;
}

export interface UpdateConceptAssignmentsRequest {
  conceptPublicIds: string[];
}

export interface CreatePayrollConfigurationRequest {
  conceptsPublicIds: string[];
}

export interface SimpleConceptDTO {
  publicId: string;
  name: string;
}

export interface CommandPayrollConfigurationResponse {
  publicId: string;
  code: string;
  createdAt: string;
  updatedAt: string;
  concepts: SimpleConceptDTO[];
}

export type PayrollConfigurationConceptAssignmentListApiResult = ApiResult<PayrollConfigurationConceptAssignmentDTO[]>;
export type PayrollConfigurationApiResult = ApiResult<CommandPayrollConfigurationResponse>;
export type VoidApiResult = ApiResult<void>;
