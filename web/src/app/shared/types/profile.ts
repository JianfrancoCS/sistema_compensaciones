import { PagedResult } from './api';

export interface Profile {
  publicId: string;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProfileDetails {
  publicId: string;
  name: string;
  description: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProfileRequest {
  name: string;
  description: string | null;
}

export interface UpdateProfileRequest {
  name: string;
  description: string | null;
}

export interface CommandProfileResponse {
  publicId: string;
  name: string;
  description: string | null;
}

export interface AssignElementsRequest {
  elementPublicIds: string[];
}

export interface ProfileElementsByContainer {
  profilePublicId: string;
  profileName: string;
  containers: ContainerWithElements[];
}

export interface ContainerWithElements {
  containerPublicId: string | null;
  containerName: string;
  containerDisplayName: string;
  containerIcon: string | null;
  elements: ElementInfo[];
  selectedElementPublicIds: string[];
}

export interface ElementInfo {
  publicId: string;
  name: string;
  displayName: string;
  route: string | null;
  icon: string | null;
}

export interface ProfileParams {
  query?: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

export type ProfileListDTO = Profile;
export type ProfileDetailsDTO = ProfileDetails;

