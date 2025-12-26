export interface AreaInfo {
  name: string;
}

export interface RequiredManagerPositionInfo {
  publicId: string;
  name: string;
}

export interface Position {
  publicId: string;
  name: string;
  area: AreaInfo;
  requiresManager: boolean;
  unique: boolean;
  requiredManagerPosition?: RequiredManagerPositionInfo | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePositionRequest {
  name: string;
  areaPublicId: string;
  salary: number;
  requiresManager?: boolean;
  unique?: boolean;
  requiredManagerPositionPublicId?: string | null;
}

export interface UpdatePositionRequest {
  name: string;
  areaPublicId: string;
  salary: number;
  requiresManager: boolean;
  unique: boolean;
  requiredManagerPositionPublicId?: string | null;
}

export interface PositionDetailsForUpdateDTO {
  publicId: string;
  name: string;
  areaPublicId: string;
  salary: number;
  requiresManager: boolean;
  unique: boolean;
  requiredManagerPositionPublicId?: string | null;
}

export interface PositionParams {
  name?: string;
  areaPublicId?: string;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}
