import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, SelectOption, StateSelectOptionDTO } from '@shared/types';
import {
  ContractParams,
  CreateContractRequest,
  UpdateContractRequest,
  GenerateUploadUrlRequest,
  AttachFileRequest,
  CancelContractRequest,
  SignContractRequest,
  ApiPagedContractsResponse,
  ApiCommandContractResponse,
  ApiContractDetailsResponse,
  ApiUploadUrlResponse,
  ApiContractSearchResponse
} from '@shared/types/contract';

@Injectable({
  providedIn: 'root'
})
export class ContractService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/hiring/contracts`;

  getContracts(params: ContractParams): Observable<ApiPagedContractsResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.contractNumber) {
      httpParams = httpParams.set('contractNumber', params.contractNumber);
    }
    if (params.contractTypePublicId) {
      httpParams = httpParams.set('contractTypePublicId', params.contractTypePublicId);
    }
    if (params.statePublicId) {
      httpParams = httpParams.set('statePublicId', params.statePublicId);
    }

    return this._http.get<ApiPagedContractsResponse>(this._url, { params: httpParams });
  }

  create(request: CreateContractRequest, photo: File): Observable<ApiResult<any>> {
    const formData = new FormData();
    formData.append('contract', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    formData.append('photo', photo);
    return this._http.post<ApiResult<any>>(this._url, formData);
  }

  update(publicId: string, request: UpdateContractRequest): Observable<ApiResult<any>> {
    return this._http.put<ApiResult<any>>(`${this._url}/${publicId}`, request);
  }

  getContractForCommand(publicId: string): Observable<ApiCommandContractResponse> {
    return this._http.get<ApiCommandContractResponse>(`${this._url}/${publicId}/command`);
  }

  getStatesSelectOptions(): Observable<ApiResult<StateSelectOptionDTO[]>> {
    return this._http.get<ApiResult<StateSelectOptionDTO[]>>(`${this._url}/states/select-options`);
  }

  getContractSelectOptions(): Observable<ApiResult<SelectOption[]>> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '250');

    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/select-options`, { params });
  }

  getContractDetails(publicId: string): Observable<ApiContractDetailsResponse> {
    return this._http.get<ApiContractDetailsResponse>(`${this._url}/${publicId}/details`);
  }

  generateUploadUrl(publicId: string, request: GenerateUploadUrlRequest): Observable<ApiUploadUrlResponse> {
    return this._http.post<ApiUploadUrlResponse>(`${this._url}/${publicId}/upload-url`, request);
  }

  attachFile(publicId: string, request: AttachFileRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(`${this._url}/${publicId}/attach-file`, request);
  }

  searchContractByNumber(contractNumber: string): Observable<ApiContractSearchResponse> {
    const params = new HttpParams().set('contractNumber', contractNumber);
    return this._http.get<ApiContractSearchResponse>(`${this._url}/search-by-number`, { params });
  }

  signContract(publicId: string, signatureFile: File): Observable<ApiResult<any>> {
    const formData = new FormData();
    formData.append('signature', signatureFile);
    return this._http.patch<ApiResult<any>>(`${this._url}/${publicId}/sign`, formData);
  }

  cancelContract(publicId: string, request: CancelContractRequest): Observable<ApiResult<any>> {
    return this._http.patch<ApiResult<any>>(`${this._url}/${publicId}/cancel`, request);
  }

  getContractContent(publicId: string): Observable<ApiResult<any>> {
    return this._http.get<ApiResult<any>>(`${this._url}/${publicId}/content`);
  }

  uploadFile(publicId: string, file: File, description?: string): Observable<ApiResult<void>> {
    const formData = new FormData();
    formData.append('file', file);
    if (description) {
      formData.append('description', description);
    }
    return this._http.post<ApiResult<void>>(`${this._url}/${publicId}/upload-file`, formData);
  }

}
