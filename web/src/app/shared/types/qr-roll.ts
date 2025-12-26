export interface QrRollPageableRequest {
  page: number;
  size: number;
  sortDirection?: 'asc' | 'desc';
  sortBy?: string;
  hasUnprintedCodes?: boolean;
}

export interface QrRollListDTO {
  publicId: string;
  maxQrCodesPerDay: number;
  totalQrCodes: number;
  unprintedQrCodes: number;
  createdAt: string;
  updatedAt: string;
  hasUnprintedCodes: boolean;
}

export interface QrCodeFilters {
  rollPublicId: string;
  isUsed?: boolean;
  isPrinted?: boolean;
}

export interface CreateQrRollRequest {
  maxQrCodesPerDay: number;
}

export interface UpdateQrRollRequest {
  maxQrCodesPerDay: number;
}

export interface GenerateQrCodesRequest {
  quantity: number;
}

export interface BatchGenerateQrCodesRequest {
  rollsNeeded: number;
  codesPerRoll: number;
}

export interface AssignRollToEmployeeRequest {
  rollPublicId: string;
  employeeDocumentNumber: string;
}

export interface QrCodeDTO {
  publicId: string;
  isUsed: boolean;
  isPrinted: boolean;
  createdAt: string;
}
