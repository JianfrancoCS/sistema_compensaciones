import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  Profile,
  CreateProfileRequest,
  UpdateProfileRequest,
  CommandProfileResponse,
  ProfileDetails,
  ProfileParams,
  AssignElementsRequest,
  ProfileElementsByContainer
} from '@shared/types/profile';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/profiles`;

  getProfiles(params: ProfileParams): Observable<ApiResult<PagedResult<Profile>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('pageSize', params.pageSize.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.query) {
      httpParams = httpParams.set('query', params.query);
    }

    return this._http.get<ApiResult<PagedResult<Profile>>>(this._url, { params: httpParams });
  }

  create(request: CreateProfileRequest): Observable<ApiResult<CommandProfileResponse>> {
    return this._http.post<ApiResult<CommandProfileResponse>>(this._url, request);
  }

  update(publicId: string, request: UpdateProfileRequest): Observable<ApiResult<CommandProfileResponse>> {
    return this._http.put<ApiResult<CommandProfileResponse>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }

  getDetails(publicId: string): Observable<ApiResult<ProfileDetails>> {
    return this._http.get<ApiResult<ProfileDetails>>(`${this._url}/${publicId}`);
  }

  getForUpdate(publicId: string): Observable<ApiResult<CommandProfileResponse>> {
    return this._http.get<ApiResult<CommandProfileResponse>>(`${this._url}/command/${publicId}`);
  }

  assignElements(publicId: string, request: AssignElementsRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(`${this._url}/${publicId}/elements`, request);
  }

  getElementsByContainer(publicId: string): Observable<ApiResult<ProfileElementsByContainer>> {
    return this._http.get<ApiResult<ProfileElementsByContainer>>(`${this._url}/${publicId}/elements-by-container`);
  }
}

