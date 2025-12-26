import { PagedResult } from './api';

export interface Container {
  publicId: string;
  name: string;
  displayName: string;
  icon: string | null;
  iconUrl: string | null;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export interface ContainerDetails {
  publicId: string;
  name: string;
  displayName: string;
  icon: string | null;
  iconUrl: string | null;
  orderIndex: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateContainerRequest {
  name: string;
  displayName: string;
  icon: string | null;
  iconUrl: string | null;
  orderIndex: number;
}

export interface UpdateContainerRequest {
  name: string;
  displayName: string;
  icon: string | null;
  iconUrl: string | null;
  orderIndex: number;
}

export interface CommandContainerResponse {
  publicId: string;
  name: string;
  displayName: string;
  icon: string | null;
  iconUrl: string | null;
  orderIndex: number;
}

export interface ContainerParams {
  query?: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export type ContainerListDTO = Container;
export type ContainerDetailsDTO = ContainerDetails;

