import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, firstValueFrom } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  UserDTO,
  CreateUserRequest,
  UpdateUserStatusRequest,
  SyncUserGroupsRequest,
  GroupAssignmentStatusDTO,
  UserParams,
  AssignProfileRequest,
  UserElementsByContainer,
  AssignUserElementsRequest,
  ProfileForAssignment,
  SyncUserProfilesRequest,
  UserDetailsDTO
} from '@shared/types/security';
import { AuthService } from './auth.service';

export interface UserProfile {
  fullName: string;
  documentNumber: string;
  positionName: string;
  subsidiaryName: string;
}

interface UserProfileApiResponse {
  code: string;
  documentNumber: string;
  names: string;
  paternalLastname: string;
  maternalLastname: string;
  dateOfBirth: string;
  gender: string;
  positionName: string;
  subsidiaryId: string;
  subsidiaryName: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly _http = inject(HttpClient);
  private readonly _authService = inject(AuthService);
  private readonly _url = `${environment.apiUrl}/v1/users`;

  getUsers(params: UserParams): Observable<ApiResult<PagedResult<UserDTO>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString());

    if (params.search) {
      httpParams = httpParams.set('search', params.search);
    }
    if (params.isActive !== undefined) {
      httpParams = httpParams.set('isActive', params.isActive.toString());
    }
    if (params.positionId) {
      httpParams = httpParams.set('positionId', params.positionId);
    }
    if (params.sortBy) {
      httpParams = httpParams.set('sortBy', params.sortBy);
    }
    if (params.sortDirection) {
      httpParams = httpParams.set('sortDirection', params.sortDirection);
    }

    return this._http.get<ApiResult<PagedResult<UserDTO>>>(this._url, { params: httpParams });
  }

  getUserDetails(userId: string): Observable<ApiResult<UserDetailsDTO>> {
    return this._http.get<ApiResult<UserDetailsDTO>>(`${this._url}/${userId}/details`);
  }

  createUser(request: CreateUserRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(this._url, request);
  }

  updateUserStatus(userId: string, request: UpdateUserStatusRequest): Observable<ApiResult<void>> {
    return this._http.patch<ApiResult<void>>(`${this._url}/${userId}/status`, request);
  }

  syncUserGroups(userId: string, request: SyncUserGroupsRequest): Observable<ApiResult<void>> {
    return this._http.put<ApiResult<void>>(`${this._url}/${userId}/groups`, request);
  }

  assignProfile(userId: string, request: AssignProfileRequest): Observable<ApiResult<void>> {
    return this._http.put<ApiResult<void>>(`${this._url}/${userId}/profile`, request);
  }

  getUserGroupStatus(userId: string): Observable<ApiResult<GroupAssignmentStatusDTO[]>> {
    return this._http.get<ApiResult<GroupAssignmentStatusDTO[]>>(`${this._url}/${userId}/groups-status`);
  }

  getElementsByContainer(userId: string): Observable<ApiResult<UserElementsByContainer>> {
    return this._http.get<ApiResult<UserElementsByContainer>>(`${this._url}/${userId}/elements-by-container`);
  }

  assignElements(userId: string, request: AssignUserElementsRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(`${this._url}/${userId}/elements`, request);
  }

  getProfilesForAssignment(userId: string): Observable<ApiResult<ProfileForAssignment[]>> {
    return this._http.get<ApiResult<ProfileForAssignment[]>>(`${this._url}/${userId}/profiles-for-assignment`);
  }

  syncUserProfiles(userId: string, request: SyncUserProfilesRequest): Observable<ApiResult<void>> {
    return this._http.put<ApiResult<void>>(`${this._url}/${userId}/profiles`, request);
  }

  async getUserProfile(): Promise<UserProfile> {
    try {
      const response = await firstValueFrom(
        this._http.get<ApiResult<UserProfileApiResponse>>(`${environment.apiUrl}/v1/auth/me`)
      );

      if (response?.success && response.data) {
        const data = response.data;
        return {
          fullName: data.names,
          documentNumber: data.documentNumber,
          positionName: data.positionName,
          subsidiaryName: data.subsidiaryName
        };
      }
    } catch (error) {
      console.log('API no disponible, usando datos del token');
    }

    return this.getUserProfileFromToken();
  }

  private getUserProfileFromToken(): UserProfile {
    const username = this._authService.getUsername() || 'Usuario';
    const groups: string[] = [];
    const firstGroup = groups.length > 0 ? groups[0] : 'Sin Grupo';

    return {
      fullName: username,
      documentNumber: 'N/A',
      positionName: firstGroup,
      subsidiaryName: 'N/A'
    };
  }
}