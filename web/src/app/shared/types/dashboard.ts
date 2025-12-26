import { ApiResult } from './api';

export interface DashboardStatsDTO {
  totalEmployees: number;
  totalPayrolls: number;
  totalPayrollsAmount: number;
  activeSubsidiaries: number;
  totalTareos: number;
  processedTareos: number;
  pendingPayrolls: number;
}

export interface PayrollsByStatusDTO {
  status: string;
  count: number;
  amount: number;
}

export interface EmployeesBySubsidiaryDTO {
  subsidiaryName: string;
  count: number;
}

export interface PayrollsByPeriodDTO {
  period: string; // "2024-10", "2024-11"
  count: number;
  totalAmount: number;
}

export interface TareosByLaborDTO {
  laborName: string;
  count: number;
  employeeCount: number;
}

export interface AttendanceTrendDTO {
  date: string;
  entries: number;
  exits: number;
}

export interface DashboardFilters {
  periodPublicId?: string;
  subsidiaryPublicId?: string;
  dateFrom?: string;
  dateTo?: string;
}

export type DashboardStatsApiResult = ApiResult<DashboardStatsDTO>;
export type PayrollsByStatusApiResult = ApiResult<PayrollsByStatusDTO[]>;
export type EmployeesBySubsidiaryApiResult = ApiResult<EmployeesBySubsidiaryDTO[]>;
export type PayrollsByPeriodApiResult = ApiResult<PayrollsByPeriodDTO[]>;
export type TareosByLaborApiResult = ApiResult<TareosByLaborDTO[]>;
export type AttendanceTrendApiResult = ApiResult<AttendanceTrendDTO[]>;

