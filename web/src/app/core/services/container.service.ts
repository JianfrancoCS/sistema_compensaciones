import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  Container,
  CreateContainerRequest,
  UpdateContainerRequest,
  CommandContainerResponse,
  ContainerDetails,
  ContainerParams
} from '@shared/types/container';

@Injectable({
  providedIn: 'root'
})
export class ContainerService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/containers`;

  getContainers(params: ContainerParams): Observable<ApiResult<PagedResult<Container>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('pageSize', params.pageSize.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.query) {
      httpParams = httpParams.set('query', params.query);
    }

    return this._http.get<ApiResult<PagedResult<Container>>>(this._url, { params: httpParams });
  }

  create(request: CreateContainerRequest): Observable<ApiResult<CommandContainerResponse>> {
    return this._http.post<ApiResult<CommandContainerResponse>>(this._url, request);
  }

  update(publicId: string, request: UpdateContainerRequest): Observable<ApiResult<CommandContainerResponse>> {
    return this._http.put<ApiResult<CommandContainerResponse>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }

  getDetails(publicId: string): Observable<ApiResult<ContainerDetails>> {
    return this._http.get<ApiResult<ContainerDetails>>(`${this._url}/${publicId}`);
  }

  getForUpdate(publicId: string): Observable<ApiResult<CommandContainerResponse>> {
    return this._http.get<ApiResult<CommandContainerResponse>>(`${this._url}/command/${publicId}`);
  }
}

