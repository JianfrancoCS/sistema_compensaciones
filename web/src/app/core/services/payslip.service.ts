import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import { PayslipListDTO, PayslipPageableRequest } from '@shared/types/payslip';

@Injectable({
  providedIn: 'root'
})
export class PayslipService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/v1/payslips`;

  list(request: PayslipPageableRequest): Observable<ApiResult<PagedResult<PayslipListDTO>>> {
    let params = new HttpParams()
      .set('page', request.page.toString())
      .set('size', request.size.toString());

    if (request.sortBy) {
      params = params.set('sortBy', request.sortBy);
    }

    if (request.sortDirection) {
      params = params.set('sortDirection', request.sortDirection);
    }

    if (request.periodFrom) {
      params = params.set('periodFrom', request.periodFrom);
    }

    if (request.periodTo) {
      params = params.set('periodTo', request.periodTo);
    }

    if (request.employeeDocumentNumber) {
      params = params.set('employeeDocumentNumber', request.employeeDocumentNumber);
    }

    return this.http.get<ApiResult<PagedResult<PayslipListDTO>>>(this.API_URL, { params });
  }

  getPdfUrl(payslipPublicId: string): string {
    return `${this.API_URL}/${payslipPublicId}/pdf`;
  }

  getPdfAsBlob(payslipPublicId: string): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${payslipPublicId}/pdf`, {
      responseType: 'blob'
    });
  }
}

