export interface PeriodDTO {
  publicId: string;
  year: number;
  month: number;
  periodStart: string;
  periodEnd: string;
  declarationDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePeriodRequest {
  explicitStartDate?: string;
}

export interface PeriodSelectOption {
  publicId: string;
  name: string;
  isAssignedToPayroll: boolean;
}

export interface PeriodState {
  periods: PeriodDTO[];
  loading: boolean;
  error: string | null;
}