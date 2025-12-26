import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '@env/environment';
import { ApiResult, StateSelectOptionDTO } from '@shared/types';
import {
  ApiCommandContractTemplateResponse,
  ApiPagedContractTemplatesResponse,
  CommandContractTemplateResponse,
  ContractTemplatePageableRequest,
  CreateContractTemplateRequest,
  UpdateContractTemplateRequest
} from '@shared/types/contract-template';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ContractTemplatesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/v1/hiring/contract-templates`;

  getContractTemplates(request: ContractTemplatePageableRequest): Observable<ApiPagedContractTemplatesResponse> {
    const params = new HttpParams()
      .set('page', request.page.toString())
      .set('size', request.size.toString())
      .set('sortBy', request.sortBy)
      .set('sortDirection', request.sortDirection)
      .set('name', request.name ?? '')
      .set('contractTypePublicId', request.contractTypePublicId ?? '')
      .set('statePublicId', request.statePublicId ?? '');
    return this.http.get<ApiPagedContractTemplatesResponse>(this.baseUrl, { params });
  }

  getStates(): Observable<ApiResult<StateSelectOptionDTO[]>> {
    return this.http.get<ApiResult<StateSelectOptionDTO[]>>(`${this.baseUrl}/states/select-options`);
  }

  getDetails(publicId: string): Observable<ApiCommandContractTemplateResponse> {
    return this.http.get<ApiCommandContractTemplateResponse>(`${this.baseUrl}/${publicId}/command`);
  }

  getContent(publicId: string): Observable<ApiCommandContractTemplateResponse> {
    return this.http.get<ApiCommandContractTemplateResponse>(`${this.baseUrl}/${publicId}/content`);
  }

  create(template: CreateContractTemplateRequest): Observable<ApiCommandContractTemplateResponse> {
    return this.http.post<ApiCommandContractTemplateResponse>(this.baseUrl, template);
  }

  update(publicId: string, template: UpdateContractTemplateRequest): Observable<ApiCommandContractTemplateResponse> {
    return this.http.put<ApiCommandContractTemplateResponse>(`${this.baseUrl}/${publicId}`, template);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this.http.delete<ApiResult<void>>(`${this.baseUrl}/${publicId}`);
  }
}
