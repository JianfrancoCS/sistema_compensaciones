import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult, SelectOption } from '@shared/types/api';
import {
  AreaListDTO,
  AreaDetailsDTO,
  CreateAreaRequest,
  UpdateAreaRequest,
  AreaParams,
  AreaSelectOptionDTO
} from '@shared/types/area';

@Injectable({
  providedIn: 'root'
})
export class AreaService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/areas`;

  getAreas(params: AreaParams): Observable<ApiResult<PagedResult<AreaListDTO>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }
    if (params.subsidiaryPublicId) {
      httpParams = httpParams.set('subsidiaryPublicId', params.subsidiaryPublicId);
    }

    return this._http.get<ApiResult<PagedResult<AreaListDTO>>>(this._url, { params: httpParams });
  }

  getSelectOptions(): Observable<ApiResult<AreaSelectOptionDTO[]>> {
    return this._http.get<ApiResult<AreaSelectOptionDTO[]>>(`${this._url}/select-options`);
  }

  create(request: CreateAreaRequest): Observable<ApiResult<any>> {
    return this._http.post<ApiResult<any>>(this._url, request);
  }

  update(publicId: string, request: UpdateAreaRequest): Observable<ApiResult<any>> {
    return this._http.put<ApiResult<any>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<any>> {
    return this._http.delete<ApiResult<any>>(`${this._url}/${publicId}`);
  }

  getDetails(publicId: string): Observable<ApiResult<AreaDetailsDTO>> {
    return this._http.get<ApiResult<AreaDetailsDTO>>(`${this._url}/${publicId}`);
  }
}
