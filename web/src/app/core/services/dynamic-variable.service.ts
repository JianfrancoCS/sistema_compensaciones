import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult } from '@shared/types/api';
import {
  DynamicVariableListDTO,
  CommandDynamicVariableResponse,
  CreateDynamicVariableRequest,
  UpdateDynamicVariableRequest,
  DynamicVariablePageableRequest,
  ApiPagedDynamicVariablesResponse
} from '@shared/types/dynamic-variable';

@Injectable({
  providedIn: 'root'
})
export class DynamicVariableService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/validation/dynamic-variables`;

  getDynamicVariables(params: DynamicVariablePageableRequest): Observable<ApiPagedDynamicVariablesResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }
    if (params.isActive !== undefined) {
      httpParams = httpParams.set('isActive', params.isActive.toString());
    }

    return this._http.get<ApiPagedDynamicVariablesResponse>(this._url, { params: httpParams });
  }

  getDynamicVariableForEdit(publicId: string): Observable<ApiResult<CommandDynamicVariableResponse>> {
    return this._http.get<ApiResult<CommandDynamicVariableResponse>>(`${this._url}/${publicId}`);
  }

  create(request: CreateDynamicVariableRequest): Observable<ApiResult<CommandDynamicVariableResponse>> {
    return this._http.post<ApiResult<CommandDynamicVariableResponse>>(this._url, request);
  }

  update(publicId: string, request: UpdateDynamicVariableRequest): Observable<ApiResult<CommandDynamicVariableResponse>> {
    return this._http.put<ApiResult<CommandDynamicVariableResponse>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }
}
