import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  LaborListDTO,
  CreateLaborRequest,
  UpdateLaborRequest,
  LaborPageableRequest,
  LaborSelectOptionDTO,
  CommandLaborResponse
} from '@shared/types/labor';

@Injectable({
  providedIn: 'root'
})
export class LaborService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/labors`;

  getAll(params: LaborPageableRequest): Observable<ApiResult<PagedResult<LaborListDTO>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }
    if (params.isPiecework !== undefined && params.isPiecework !== null) {
      httpParams = httpParams.set('isPiecework', params.isPiecework.toString());
    }
    if (params.laborUnitPublicId) {
      httpParams = httpParams.set('laborUnitPublicId', params.laborUnitPublicId);
    }

    return this._http.get<ApiResult<PagedResult<LaborListDTO>>>(this._url, { params: httpParams });
  }

  getSelectOptions(): Observable<ApiResult<LaborSelectOptionDTO[]>> {
    return this._http.get<ApiResult<LaborSelectOptionDTO[]>>(`${this._url}/select-options`);
  }

  create(request: CreateLaborRequest): Observable<ApiResult<CommandLaborResponse>> {
    return this._http.post<ApiResult<CommandLaborResponse>>(this._url, request);
  }

  update(publicId: string, request: UpdateLaborRequest): Observable<ApiResult<CommandLaborResponse>> {
    return this._http.put<ApiResult<CommandLaborResponse>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }
}
