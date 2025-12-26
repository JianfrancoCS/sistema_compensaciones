export interface ApiResult<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: { [key: string]: string };
  timeStamp: string;
}

export interface PagedResult<T> {
  data: T[];
  totalElements: number;
  pageNumber: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface SelectOption {
  publicId: string;
  name: string;
}

export interface StateSelectOptionDTO extends SelectOption{
  default: boolean;
}
