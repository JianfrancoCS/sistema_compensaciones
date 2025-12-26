import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, SelectOption } from '@shared/types/api';
import {
  CreateContractTypeRequest,
  UpdateContractTypeRequest,
  ApiCommandContractTypeResponse,
  ApiContractTypeSelectOptionsResponse
} from '@shared/types/contract-type';

@Injectable({
  providedIn: 'root'
})
export class ContractTypeService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/hiring/contract-types`;

  getContractTypeSelectOptions(): Observable<ApiContractTypeSelectOptionsResponse> {
    return this._http.get<ApiContractTypeSelectOptionsResponse>(`${this._url}/select-options`);
  }

  create(request: CreateContractTypeRequest): Observable<ApiCommandContractTypeResponse> {
    return this._http.post<ApiCommandContractTypeResponse>(this._url, request);
  }

  update(publicId: string, request: UpdateContractTypeRequest): Observable<ApiCommandContractTypeResponse> {
    return this._http.put<ApiCommandContractTypeResponse>(`${this._url}/${publicId}`, request);
  }

  getContractTypeForCommand(publicId: string): Observable<ApiCommandContractTypeResponse> {
    return this._http.get<ApiCommandContractTypeResponse>(`${this._url}/command/${publicId}`);
  }
}
