import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult } from '@shared/types/api';
import { PeriodDTO, CreatePeriodRequest, PeriodSelectOption } from '@shared/types/period';

@Injectable({
  providedIn: 'root'
})
export class PeriodService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/periods`;

  getAll(): Observable<ApiResult<PeriodDTO[]>> {
    return this._http.get<ApiResult<PeriodDTO[]>>(this._url);
  }

  getSelectOptions(): Observable<ApiResult<PeriodSelectOption[]>> {
    return this._http.get<ApiResult<PeriodSelectOption[]>>(`${this._url}/select-options`);
  }

  create(request: CreatePeriodRequest): Observable<ApiResult<PeriodDTO>> {
    return this._http.post<ApiResult<PeriodDTO>>(this._url, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }
}