import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResult } from '@shared/types/api';
import {
  CompanyDTO,
  CreateCompanyRequest,
  UpdateCompanyRequest,
  UpdateMaxMonthlyWorkingHoursRequest,
  UpdatePaymentIntervalDaysRequest,
  UpdatePayrollDeclarationDayRequest,
  CompanyExternalInfo
} from '@shared/types/company';
import {environment} from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/v1/company`;

  getCompany(): Observable<ApiResult<CompanyDTO>> {
    return this.http.get<ApiResult<CompanyDTO>>(this.apiUrl);
  }

  create(request: CreateCompanyRequest): Observable<ApiResult<CompanyDTO>> {
    return this.http.post<ApiResult<CompanyDTO>>(this.apiUrl, request);
  }

  update(request: UpdateCompanyRequest): Observable<ApiResult<CompanyDTO>> {
    return this.http.put<ApiResult<CompanyDTO>>(this.apiUrl, request);
  }

  updatePayrollDeclarationDay(request: UpdatePayrollDeclarationDayRequest): Observable<ApiResult<CompanyDTO>> {
    return this.http.patch<ApiResult<CompanyDTO>>(`${this.apiUrl}/payroll-declaration-day`, request);
  }

  updatePaymentIntervalDays(request: UpdatePaymentIntervalDaysRequest): Observable<ApiResult<CompanyDTO>> {
    return this.http.patch<ApiResult<CompanyDTO>>(`${this.apiUrl}/payment-interval-days`, request);
  }

  updateMaxMonthlyWorkingHours(request: UpdateMaxMonthlyWorkingHoursRequest): Observable<ApiResult<CompanyDTO>> {
    return this.http.patch<ApiResult<CompanyDTO>>(`${this.apiUrl}/max-monthly-working-hours`, request);
  }

  externalLookup(ruc: string): Observable<ApiResult<CompanyExternalInfo>> {
    return this.http.get<ApiResult<CompanyExternalInfo>>(`${this.apiUrl}/external-lookup/${ruc}`);
  }
}
