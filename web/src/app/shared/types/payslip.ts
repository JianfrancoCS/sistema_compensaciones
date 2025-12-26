import { ApiResult, PagedResult } from './api';

export interface PayslipListDTO {
  publicId: string;
  payrollPublicId: string;
  payrollCode: string;
  employeeDocumentNumber: string;
  employeeNames: string;
  employeeFullName: string;
  subsidiaryName: string;
  periodName: string;
  periodStart: string;
  periodEnd: string;
  totalIncome: number;
  totalDeductions: number;
  netToPay: number;
  createdAt: string;
  payslipPdfUrl?: string | null; // URL del PDF almacenado (Cloudinary o similar)
}

export interface PayslipPageableRequest {
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  periodFrom?: string;
  periodTo?: string;
  employeeDocumentNumber?: string;
}

export type PayslipListApiResult = ApiResult<PagedResult<PayslipListDTO>>;

