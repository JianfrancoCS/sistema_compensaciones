import { SelectOption } from './api';

export interface DistrictDetailResponseDTO {
  publicId: string;
  name: string;
  ubigeoReniec: string;
  ubigeoInei: string;
  provincePublicId: string;
  departmentPublicId: string;
}

export interface DistrictSelectOptionDTO extends SelectOption {
}