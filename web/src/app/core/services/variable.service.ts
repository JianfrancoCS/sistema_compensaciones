import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult } from '@shared/types/api';
import {
  VariableSelectOption,
  CommandVariableResponse,
  CreateVariableRequest,
  UpdateVariableRequest,
  CreateVariableWithValidationRequest,
  UpdateVariableWithValidationRequest,
  VariableParams,
  ApiPagedVariablesResponse,
  ValidationMethodDTO,
  VariableValidationRequest,
  VariableValidationDTO,
  ApiValidationMethodsResponse,
  ApiVariableValidationResponse,
  VariableSelectOptionsParams
} from '@shared/types/variable';

@Injectable({
  providedIn: 'root'
})
export class VariableService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/hiring/variables`;

  getVariables(params: VariableParams): Observable<ApiPagedVariablesResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }
    if (params.code) {
      httpParams = httpParams.set('code', params.code);
    }

    return this._http.get<ApiPagedVariablesResponse>(this._url, { params: httpParams });
  }

  getSelectOptions(params?: VariableSelectOptionsParams): Observable<ApiResult<VariableSelectOption[]>> {
    let httpParams = new HttpParams();
    if (params?.name) {
      httpParams = httpParams.set('name', params.name);
    }
    return this._http.get<ApiResult<VariableSelectOption[]>>(`${this._url}/select-options`, { params: httpParams });
  }

  create(request: CreateVariableRequest): Observable<ApiResult<CommandVariableResponse>> {
    return this._http.post<ApiResult<CommandVariableResponse>>(this._url, request);
  }

  createWithValidation(request: CreateVariableWithValidationRequest): Observable<ApiResult<CommandVariableResponse>> {
    return this._http.post<ApiResult<CommandVariableResponse>>(`${this._url}/with-validation`, request);
  }

  update(publicId: string, request: UpdateVariableRequest): Observable<ApiResult<CommandVariableResponse>> {
    return this._http.put<ApiResult<CommandVariableResponse>>(`${this._url}/${publicId}`, request);
  }

  updateWithValidation(publicId: string, request: UpdateVariableWithValidationRequest): Observable<ApiResult<CommandVariableResponse>> {
    return this._http.put<ApiResult<CommandVariableResponse>>(`${this._url}/${publicId}/with-validation`, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }

  getValidationMethods(): Observable<ApiValidationMethodsResponse> {
    return this._http.get<ApiValidationMethodsResponse>(`${this._url}/validation-methods`);
  }

  associateValidationMethods(variableId: string, request: VariableValidationRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(`${this._url}/${variableId}/methods`, request);
  }

  updateValidationMethods(variableId: string, request: VariableValidationRequest): Observable<ApiResult<void>> {
    return this._http.put<ApiResult<void>>(`${this._url}/${variableId}/methods`, request);
  }

  removeValidationMethods(variableId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${variableId}/methods`);
  }

  getVariableWithValidation(variableId: string): Observable<ApiVariableValidationResponse> {
    return this._http.get<ApiVariableValidationResponse>(`${this._url}/${variableId}/validation`);
  }
}
