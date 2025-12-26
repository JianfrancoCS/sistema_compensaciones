import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import {
  ApiCommandLaborUnitResponse,
  ApiLaborUnitDetailsResponse,
  ApiLaborUnitSelectOptionsResponse,
  ApiPagedLaborUnitsResponse,
  CreateLaborUnitRequest,
  LaborUnitParams,
  UpdateLaborUnitRequest
} from '@shared/types/labor-unit';

@Injectable({ providedIn: 'root' })
export class LaborUnitService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/v1/labor-units`;

  getLaborUnits(params: LaborUnitParams): Observable<ApiPagedLaborUnitsResponse> {
    return this.http.get<ApiPagedLaborUnitsResponse>(this.apiUrl, { params: params as any });
  }

  getDetails(publicId: string): Observable<ApiLaborUnitDetailsResponse> {
    return this.http.get<ApiLaborUnitDetailsResponse>(`${this.apiUrl}/${publicId}`);
  }

  getSelectOptions(): Observable<ApiLaborUnitSelectOptionsResponse> {
    return this.http.get<ApiLaborUnitSelectOptionsResponse>(`${this.apiUrl}/select-options`);
  }

  create(request: CreateLaborUnitRequest): Observable<ApiCommandLaborUnitResponse> {
    return this.http.post<ApiCommandLaborUnitResponse>(this.apiUrl, request);
  }

  update(publicId: string, request: UpdateLaborUnitRequest): Observable<ApiCommandLaborUnitResponse> {
    return this.http.put<ApiCommandLaborUnitResponse>(`${this.apiUrl}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiCommandLaborUnitResponse> {
    return this.http.delete<ApiCommandLaborUnitResponse>(`${this.apiUrl}/${publicId}`);
  }
}
