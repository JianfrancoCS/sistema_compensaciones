import { ApiResult, PagedResult } from './api';
import { TareoListDTO } from './tareo';

export type PayrollStatus = 'BORRADOR' | 'CALCULANDO' | 'CALCULADA' | 'CERRADA' | 'ANULADA';

export interface PayrollListDTO {
  publicId: string;
  code: string;
  subsidiaryPublicId?: string;
  subsidiaryName: string;
  periodPublicId?: string;
  periodName: string;
  periodMonth?: number;
  periodYear?: number;
  status?: PayrollStatus;
  stateName: string;
  totalEmployees: number;
  processedTareos: number; // Cantidad de tareos procesados
  hasPayslips: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CommandPayrollResponse {
  publicId: string;
  subsidiaryPublicId: string;
  periodPublicId: string;
  status: PayrollStatus;
  createdAt: string;
  updatedAt: string;
}

export interface PayrollSummaryDTO {
  publicId: string;
  code: string;
  subsidiaryName: string;
  subsidiaryPublicId: string;
  year: number;
  month: number;
  periodLabel: string;
  totalEmployees: number;
  totalIncome: number;
  totalDeductions: number;
  totalEmployerContributions: number;
  totalNet: number;
  totalHealth: number; // Total en salud
  totalRetirement: number; // Total en jubilación
  totalRemuneration: number; // Total en remuneración
  totalBonus: number; // Total en bonos
  incomeConcepts: ConceptSummary[];
  deductionConcepts: ConceptSummary[];
  employerContributionConcepts: ConceptSummary[];
}

export interface ConceptSummary {
  conceptCode: string;
  conceptName: string;
  totalAmount: number;
  category: string;
}

export interface CreatePayrollRequest {
  subsidiaryPublicId: string;
  payrollPeriodPublicId: string;
}

export interface PayrollPageableRequest {
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  subsidiaryPublicId?: string;
  periodPublicId?: string;
  status?: string;
}

export interface PayrollEmployeeListDTO {
  publicId: string;
  employeeDocumentNumber: string;
  employeeFullName: string;
  positionName: string;
  totalIncome: number;
  totalDeductions: number;
  netToPay: number;
  daysWorked: number;
}

export interface PayrollEmployeeDetailDTO {
  publicId: string;
  employeeDocumentNumber: string;
  employeeFullName: string;
  positionName: string;
  totalIncome: number;
  totalDeductions: number;
  totalEmployerContributions: number;
  netToPay: number;
  daysWorked: number;
  normalHours: number;
  overtimeHours25: number;
  overtimeHours35: number;
  overtimeHours100: number;
  nightHours: number;
  calculatedConcepts: Record<string, any>;
  dailyDetails: DailyWorkDetail[];
}

export interface DailyWorkDetail {
  date: string;
  dayOfWeek: string;
  hours: number;
  nightHours: number;
  performancePercentage: number; // Porcentaje de productividad
  productivityValue: number | null; // Valor numérico de productividad (harvestCount)
  productivityUnit: string | null; // Unidad de medida (ej: "Jarras", "Jabas")
  isHoliday: boolean; // Indica si es feriado (tiene evento HOLIDAY)
  isNonWorkingDay: boolean; // Indica si es día no laborable (domingo o feriado)
  worked: boolean; // Indica si el empleado trabajó ese día
}

export type PayrollListApiResult = ApiResult<PagedResult<PayrollListDTO>>;
export type CommandPayrollApiResult = ApiResult<CommandPayrollResponse>;
export type PayrollSummaryApiResult = ApiResult<PayrollSummaryDTO>;
export type VoidApiResult = ApiResult<void>;
export type ProcessedTareosApiResult = ApiResult<TareoListDTO[]>;
export type PayrollEmployeesApiResult = ApiResult<PayrollEmployeeListDTO[]>;
export type PayrollEmployeeDetailApiResult = ApiResult<PayrollEmployeeDetailDTO>;