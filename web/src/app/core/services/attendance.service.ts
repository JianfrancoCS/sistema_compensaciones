import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, signal, inject } from '@angular/core';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, SelectOption } from '@shared/types/api';
import {
  ExternalMarkRequest,
  EmployeeMarkRequest,
  MarkingResponse,
  ApiMarkingResponse
} from '@shared/types/attendance';

@Injectable({
  providedIn: 'root'
})
export class AttendanceService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/attendance`;

  public markExternal(request: ExternalMarkRequest): Observable<ApiMarkingResponse> {
    return this._http.post<ApiMarkingResponse>(`${this._url}/markings/external`, request);
  }

  public markEmployee(request: EmployeeMarkRequest): Observable<ApiMarkingResponse> {
    return this._http.post<ApiMarkingResponse>(`${this._url}/markings/employee`, request);
  }

  public getMarkingReasonsSelectOptions(isInternal: boolean): Observable<ApiResult<SelectOption[]>> {
    const params = new HttpParams().set('isInternal', isInternal.toString());
    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/marking-reasons/select-options`, { params });
  }

  public getEmployeeMarkingReasonsSelectOptions(): Observable<ApiResult<SelectOption[]>> {
    return this.getMarkingReasonsSelectOptions(true);
  }

  public getExternalMarkingReasonsSelectOptions(): Observable<ApiResult<SelectOption[]>> {
    return this.getMarkingReasonsSelectOptions(false);
  }
}
