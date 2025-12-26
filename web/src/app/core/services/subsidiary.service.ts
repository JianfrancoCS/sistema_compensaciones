import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult, SelectOption } from '@shared/types/api';
import {
  SubsidiaryListDTO,
  SubsidiaryDetailsDTO,
  CreateSubsidiaryRequest,
  UpdateSubsidiaryRequest,
  SubsidiaryParams
} from '@shared/types/subsidiary';

@Injectable({
  providedIn: 'root'
})
export class SubsidiaryService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/subsidiaries`;

  getSubsidiaries(params: SubsidiaryParams): Observable<ApiResult<PagedResult<SubsidiaryListDTO>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }

    return this._http.get<ApiResult<PagedResult<SubsidiaryListDTO>>>(this._url, { params: httpParams });
  }

  getSelectOptions(): Observable<ApiResult<SelectOption[]>> {
    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/select-options`);
  }

  create(request: CreateSubsidiaryRequest): Observable<ApiResult<any>> {
    return this._http.post<ApiResult<any>>(this._url, request);
  }

  update(publicId: string, request: UpdateSubsidiaryRequest): Observable<ApiResult<any>> {
    return this._http.put<ApiResult<any>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<any>> {
    return this._http.delete<ApiResult<any>>(`${this._url}/${publicId}`);
  }

  getDetails(publicId: string): Observable<ApiResult<SubsidiaryDetailsDTO>> {
    return this._http.get<ApiResult<SubsidiaryDetailsDTO>>(`${this._url}/${publicId}`);
  }
}
