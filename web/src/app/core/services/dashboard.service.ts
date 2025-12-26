import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import {
  DashboardStatsDTO,
  DashboardFilters,
  DashboardStatsApiResult,
  PayrollsByStatusApiResult,
  EmployeesBySubsidiaryApiResult,
  PayrollsByPeriodApiResult,
  TareosByLaborApiResult,
  AttendanceTrendApiResult
} from '@shared/types/dashboard';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/v1/dashboard`;

  getStats(filters?: DashboardFilters): Observable<DashboardStatsApiResult> {
    let params = new HttpParams();
    
    if (filters?.periodPublicId) {
      params = params.set('periodPublicId', filters.periodPublicId);
    }
    
    if (filters?.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', filters.subsidiaryPublicId);
    }
    
    if (filters?.dateFrom) {
      params = params.set('dateFrom', filters.dateFrom);
    }
    
    if (filters?.dateTo) {
      params = params.set('dateTo', filters.dateTo);
    }

    return this.http.get<DashboardStatsApiResult>(`${this.API_URL}/stats`, { params });
  }

  getPayrollsByStatus(filters?: DashboardFilters): Observable<PayrollsByStatusApiResult> {
    let params = new HttpParams();
    
    if (filters?.periodPublicId) {
      params = params.set('periodPublicId', filters.periodPublicId);
    }
    
    if (filters?.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', filters.subsidiaryPublicId);
    }

    return this.http.get<PayrollsByStatusApiResult>(`${this.API_URL}/payrolls-by-status`, { params });
  }

  getEmployeesBySubsidiary(filters?: DashboardFilters): Observable<EmployeesBySubsidiaryApiResult> {
    let params = new HttpParams();
    
    if (filters?.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', filters.subsidiaryPublicId);
    }

    return this.http.get<EmployeesBySubsidiaryApiResult>(`${this.API_URL}/employees-by-subsidiary`, { params });
  }

  getPayrollsByPeriod(filters?: DashboardFilters): Observable<PayrollsByPeriodApiResult> {
    let params = new HttpParams();
    
    if (filters?.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', filters.subsidiaryPublicId);
    }
    
    if (filters?.dateFrom) {
      params = params.set('dateFrom', filters.dateFrom);
    }
    
    if (filters?.dateTo) {
      params = params.set('dateTo', filters.dateTo);
    }

    return this.http.get<PayrollsByPeriodApiResult>(`${this.API_URL}/payrolls-by-period`, { params });
  }

  getTareosByLabor(filters?: DashboardFilters): Observable<TareosByLaborApiResult> {
    let params = new HttpParams();
    
    if (filters?.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', filters.subsidiaryPublicId);
    }
    
    if (filters?.dateFrom) {
      params = params.set('dateFrom', filters.dateFrom);
    }
    
    if (filters?.dateTo) {
      params = params.set('dateTo', filters.dateTo);
    }

    return this.http.get<TareosByLaborApiResult>(`${this.API_URL}/tareos-by-labor`, { params });
  }

  getAttendanceTrend(filters?: DashboardFilters): Observable<AttendanceTrendApiResult> {
    let params = new HttpParams();
    
    if (filters?.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', filters.subsidiaryPublicId);
    }
    
    if (filters?.dateFrom) {
      params = params.set('dateFrom', filters.dateFrom);
    }
    
    if (filters?.dateTo) {
      params = params.set('dateTo', filters.dateTo);
    }

    return this.http.get<AttendanceTrendApiResult>(`${this.API_URL}/attendance-trend`, { params });
  }
}

