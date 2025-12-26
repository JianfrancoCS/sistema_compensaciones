import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  JustificationListDTO,
  JustificationDetailsDTO,
  CreateJustificationRequest,
  UpdateJustificationRequest,
  JustificationPageableRequest,
  JustificationSelectOptionDTO
} from '@shared/types/justification';

@Injectable({
  providedIn: 'root'
})
export class JustificationService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/tareo-motives`;

  getJustifications(params: JustificationPageableRequest): Observable<ApiResult<PagedResult<JustificationListDTO>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }
    if (params.isPaid !== undefined) {
      httpParams = httpParams.set('isPaid', params.isPaid.toString());
    }

    return this._http.get<ApiResult<PagedResult<JustificationListDTO>>>(this._url, { params: httpParams });
  }

  getSelectOptions(): Observable<ApiResult<JustificationSelectOptionDTO[]>> {
    return this._http.get<ApiResult<JustificationSelectOptionDTO[]>>(`${this._url}/select-options`);
  }

  create(request: CreateJustificationRequest): Observable<ApiResult<any>> {
    return this._http.post<ApiResult<any>>(this._url, request);
  }

  update(publicId: string, request: UpdateJustificationRequest): Observable<ApiResult<any>> {
    return this._http.put<ApiResult<any>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<any>> {
    return this._http.delete<ApiResult<any>>(`${this._url}/${publicId}`);
  }

  getDetails(publicId: string): Observable<ApiResult<JustificationDetailsDTO>> {
    return this._http.get<ApiResult<JustificationDetailsDTO>>(`${this._url}/${publicId}`);
  }
}
