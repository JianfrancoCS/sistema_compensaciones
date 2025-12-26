import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  BatchPageableRequest,
  ApiResultPagedBatchListDTO,
  CreateBatchRequest,
  ApiResultCommandBatchResponse,
  UpdateBatchRequest,
  ApiResultVoid,
  ApiResultBatchSelectOptionDTOList,
  ApiResultBatchDetailsDTO
} from '@shared/types/batches';
import {environment} from '@env/environment';

@Injectable({
  providedIn: 'root',
})
export class BatchService {
  private apiUrl = `${environment.apiUrl}/v1/lotes`;

  constructor(private http: HttpClient) {}

  getBatches(params: BatchPageableRequest): Observable<ApiResultPagedBatchListDTO> {
    let httpParams = new HttpParams();
    if (params.page !== undefined) httpParams = httpParams.append('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.append('size', params.size.toString());
    if (params.sortBy) httpParams = httpParams.append('sortBy', params.sortBy);
    if (params.sortDirection) httpParams = httpParams.append('sortDirection', params.sortDirection);
    if (params.name) httpParams = httpParams.append('name', params.name);
    if (params.subsidiaryPublicId) httpParams = httpParams.append('subsidiaryPublicId', params.subsidiaryPublicId);

    return this.http.get<ApiResultPagedBatchListDTO>(this.apiUrl, { params: httpParams });
  }

  getBatchDetails(publicId: string): Observable<ApiResultBatchDetailsDTO> {
    return this.http.get<ApiResultBatchDetailsDTO>(`${this.apiUrl}/${publicId}`);
  }

  createBatch(request: CreateBatchRequest): Observable<ApiResultCommandBatchResponse> {
    return this.http.post<ApiResultCommandBatchResponse>(this.apiUrl, request);
  }

  updateBatch(publicId: string, request: UpdateBatchRequest): Observable<ApiResultCommandBatchResponse> {
    return this.http.put<ApiResultCommandBatchResponse>(`${this.apiUrl}/${publicId}`, request);
  }

  deleteBatch(publicId: string): Observable<ApiResultVoid> {
    return this.http.delete<ApiResultVoid>(`${this.apiUrl}/${publicId}`);
  }

  getSelectOptions(): Observable<ApiResultBatchSelectOptionDTOList> {
    return this.http.get<ApiResultBatchSelectOptionDTOList>(`${this.apiUrl}/select-options`);
  }
}
