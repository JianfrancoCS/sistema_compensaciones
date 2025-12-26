export interface CompanyDTO {
  publicId: string;
  legalName: string;
  tradeName: string;
  ruc: string;
  companyType: string;
  logoUrl?: string | null;
  paymentIntervalDays: number;
  maxMonthlyWorkingHours: number | null;
  payrollDeclarationDay: number;
  payrollAnticipationDays: number;
  overtimeRate: number;
  dailyNormalHours: number;
  monthCalculationDays: number;
  createdAt: string;
  updatedAt: string;
}

export interface CompanyExternalInfo {
  ruc: string;
  businessName: string;
  tradeName: string;
  status: string;
  companyType: string;
}

export interface CreateCompanyRequest {
  legalName: string;
  tradeName: string;
  ruc: string;
  companyType: string;
  paymentIntervalDays: number;
  maxMonthlyWorkingHours: number | null;
  payrollDeclarationDay: number;
  payrollAnticipationDays: number;
  overtimeRate: number;
  dailyNormalHours: number;
  monthCalculationDays: number;
}

export interface UpdateCompanyRequest extends CreateCompanyRequest {}

export interface UpdatePayrollDeclarationDayRequest {
  payrollDeclarationDay: number;
}

export interface UpdatePaymentIntervalDaysRequest {
  paymentIntervalDays: number;
}

export interface UpdateMaxMonthlyWorkingHoursRequest {
  maxMonthlyWorkingHours: number;
}

export interface CompanyState {
  company: CompanyDTO | null;
  loading: boolean;
  error: string | null;
  externalLookupLoading: boolean;
  externalCompanyInfo: CompanyExternalInfo | null;
}
