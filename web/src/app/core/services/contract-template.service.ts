import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, SelectOption } from '@shared/types/api';
import {
  ContractTemplatePageableRequest,
  CreateContractTemplateRequest,
  UpdateContractTemplateRequest,
  ApiPagedContractTemplatesResponse,
  ApiCommandContractTemplateResponse,
  ApiContractTemplateSelectOptionsResponse
} from '@shared/types/contract-template';
import { ApiContractVariablesWithValidationResponse } from '@shared/types/variable';

@Injectable({
  providedIn: 'root'
})
export class ContractTemplateService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/hiring/contract-templates`;

  getContractTemplates(params: ContractTemplatePageableRequest): Observable<ApiPagedContractTemplatesResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }
    if (params.contractTypePublicId) {
      httpParams = httpParams.set('contractTypePublicId', params.contractTypePublicId);
    }

    return this._http.get<ApiPagedContractTemplatesResponse>(this._url, { params: httpParams });
  }

  getContractTemplateForCommand(publicId: string): Observable<ApiCommandContractTemplateResponse> {
    return this._http.get<ApiCommandContractTemplateResponse>(`${this._url}/${publicId}/command`);
  }

  create(request: CreateContractTemplateRequest): Observable<ApiResult<any>> {
    return this._http.post<ApiResult<any>>(this._url, request);
  }

  update(publicId: string, request: UpdateContractTemplateRequest): Observable<ApiResult<any>> {
    return this._http.put<ApiResult<any>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<any>> {
    return this._http.delete<ApiResult<any>>(`${this._url}/${publicId}`);
  }

  getContractTemplateSelectOptions(contractTypePublicId?: string): Observable<ApiContractTemplateSelectOptionsResponse> {
    let params = new HttpParams();
    if (contractTypePublicId) {
      params = params.set('contractTypePublicId', contractTypePublicId);
    }
    return this._http.get<ApiContractTemplateSelectOptionsResponse>(`${this._url}/select-options`, { params });
  }

  getVariablesWithValidation(templatePublicId: string): Observable<ApiContractVariablesWithValidationResponse> {
    return this._http.get<ApiContractVariablesWithValidationResponse>(`${this._url}/${templatePublicId}/variables-with-validation`);
  }
}
