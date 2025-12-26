import { BasePageableRequest } from '@shared/types/base';

export interface JustificationListDTO {
  publicId: string;
  name: string;
  description: string;
  isPaid: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface JustificationDetailsDTO {
  publicId: string;
  name: string;
  description: string;
  isPaid: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface JustificationPageableRequest extends BasePageableRequest {
  name?: string;
  isPaid?: boolean;
}

export interface CreateJustificationRequest {
  name: string;
  description?: string;
  isPaid: boolean;
}

export interface UpdateJustificationRequest {
  name: string;
  description?: string;
  isPaid: boolean;
}

export interface JustificationSelectOptionDTO {
  publicId: string;
  name: string;
  isPaid: boolean;
}

export interface JustificationState {
  justifications: JustificationListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  selectOptions: JustificationSelectOptionDTO[];
  filters: JustificationPageableRequest;
}
