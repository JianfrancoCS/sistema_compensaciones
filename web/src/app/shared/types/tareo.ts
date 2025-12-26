import { ApiResult, PagedResult } from './api';

export interface TareoListDTO {
  publicId: string;
  laborName: string;
  loteName: string;
  loteSubsidiaryName: string;
  employeeCount: number;
  isProcessed: boolean;
  createdAt: string;
}

export interface TareoDailyDTO {
  publicId: string;
  tareoDate: string; // ISO date string
  laborName: string;
  laborPublicId: string;
  loteName: string;
  lotePublicId: string;
  subsidiaryName: string;
  subsidiaryPublicId: string;
  supervisorName: string | null;
  supervisorDocumentNumber: string;
  scannerName: string | null;
  scannerDocumentNumber: string | null;
  employeeCount: number;
  isCalculated: boolean;
  createdAt: string;
}

export interface TareoPageableRequest {
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  laborPublicId?: string;
  subsidiaryPublicId?: string;
  createdBy?: string;
  dateFrom?: string; // ISO date string
  dateTo?: string; // ISO date string
  isProcessed?: boolean;
}

export interface TareoDailyPageableRequest {
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  laborPublicId?: string;
  subsidiaryPublicId?: string;
  dateFrom?: string;
  dateTo?: string;
  isCalculated?: boolean;
}

export interface TareoDetailDTO {
  publicId: string;
  labor: {
    name: string;
    isPiecework: boolean;
  };
  lote: {
    loteName: string;
    subsidiaryName: string;
  };
  supervisor: {
    documentNumber: string;
    fullName: string | null;
  };
  acopiador: {
    documentNumber: string;
    fullName: string | null;
  } | null;
  createdBy: string;
  createdAt: string;
  employees: TareoEmployeeItem[];
}

export interface TareoEmployeeItem {
  publicId: string;
  documentNumber: string;
  fullName: string | null;
  position: string | null;
  entryTime: string | null; // LocalTime as string (HH:mm:ss)
  exitTime: string | null; // LocalTime as string (HH:mm:ss)
  productivity: TareoProductivityInfo | null;
}

export interface TareoProductivityInfo {
  productivityPercentage: number;
  harvestCount: number;
  minTaskRequirement: number;
  unitOfMeasure: string | null; // Unidad de medida (ej: "Jarras", "Jabas")
  collector: {
    documentNumber: string;
    fullName: string | null;
  };
}

export type TareoListApiResult = ApiResult<PagedResult<TareoListDTO>>;
export type TareoDailyListApiResult = ApiResult<PagedResult<TareoDailyDTO>>;
export type TareoDetailApiResult = ApiResult<TareoDetailDTO>;
export type VoidApiResult = ApiResult<void>;

