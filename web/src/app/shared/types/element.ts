import { PagedResult } from './api';

export interface Element {
  publicId: string;
  name: string;
  displayName: string;
  route: string | null;
  icon: string | null;
  iconUrl: string | null;
  container: ContainerInfo | null;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export interface ContainerInfo {
  publicId: string;
  name: string;
  displayName: string;
}

export interface ElementDetails {
  publicId: string;
  name: string;
  displayName: string;
  route: string | null;
  icon: string | null;
  iconUrl: string | null;
  container: ContainerInfo | null;
  orderIndex: number;
  isActive: boolean;
  isWeb: boolean;
  isMobile: boolean;
  isDesktop: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateElementRequest {
  name: string;
  displayName: string;
  route: string | null;
  icon: string | null;
  iconUrl: string | null;
  containerPublicId: string | null;
  orderIndex: number;
  isWeb?: boolean;
  isMobile?: boolean;
  isDesktop?: boolean;
}

export interface UpdateElementRequest {
  name: string;
  displayName: string;
  route: string | null;
  icon: string | null;
  iconUrl: string | null;
  containerPublicId: string | null;
  orderIndex: number;
  isWeb?: boolean;
  isMobile?: boolean;
  isDesktop?: boolean;
}

export interface CommandElementResponse {
  publicId: string;
  name: string;
  displayName: string;
  route: string | null;
  icon: string | null;
  iconUrl: string | null;
  containerPublicId: string | null;
  orderIndex: number;
  isWeb?: boolean;
  isMobile?: boolean;
  isDesktop?: boolean;
}

export interface ElementParams {
  query?: string;
  containerPublicId?: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export type ElementListDTO = Element;
export type ElementDetailsDTO = ElementDetails;

