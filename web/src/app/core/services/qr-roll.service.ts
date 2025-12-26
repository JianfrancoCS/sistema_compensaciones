import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  QrRollListDTO,
  QrCodeDTO,
  UpdateQrRollRequest,
  GenerateQrCodesRequest,
  BatchGenerateQrCodesRequest,
  AssignRollToEmployeeRequest,
  QrRollPageableRequest,
  QrCodeFilters,
  CreateQrRollRequest
} from '@shared/types/qr-roll';

@Injectable({
  providedIn: 'root'
})
export class QrRollService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/qr-rolls`;

  getAll(params: QrRollPageableRequest): Observable<ApiResult<PagedResult<QrRollListDTO>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString());

    if (params.sortBy) {
      httpParams = httpParams.set('sortBy', params.sortBy);
    }
    if (params.sortDirection) {
      httpParams = httpParams.set('sortDirection', params.sortDirection);
    }
    if (params.hasUnprintedCodes ) {
      httpParams = httpParams.set('hasUnprintedCodes', params.hasUnprintedCodes.toString());
    }

    return this._http.get<ApiResult<PagedResult<QrRollListDTO>>>(this._url, { params: httpParams });
  }

  create(request: CreateQrRollRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(this._url, request);
  }

  update(publicId: string, request: UpdateQrRollRequest): Observable<ApiResult<void>> {
    return this._http.put<ApiResult<void>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }

  print(rollPublicId: string): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(`${this._url}/${rollPublicId}/print`, {});
  }

  generateCodes(rollPublicId: string, request: GenerateQrCodesRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(`${this._url}/${rollPublicId}/generate-codes`, request);
  }

  batchGenerate(request: BatchGenerateQrCodesRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(`${this._url}/batch-generate`, request);
  }

  assignToEmployee(request: AssignRollToEmployeeRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(`${this._url}/assign`, request);
  }

  getQrCodes(filters: QrCodeFilters): Observable<ApiResult<QrCodeDTO[]>> {
    let httpParams = new HttpParams();

    if (filters.isUsed ) {
      httpParams = httpParams.set('isUsed', filters.isUsed.toString());
    }
    if (filters.isPrinted ) {
      httpParams = httpParams.set('isPrinted', filters.isPrinted.toString());
    }

    return this._http.get<ApiResult<QrCodeDTO[]>>(
      `${this._url}/${filters.rollPublicId}/qr-codes`,
      { params: httpParams }
    );
  }
}
