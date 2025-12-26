import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { PayrollConfigurationConceptAssignmentListApiResult, UpdateConceptAssignmentsRequest, PayrollConfigurationApiResult, CreatePayrollConfigurationRequest, VoidApiResult } from '@shared/types/payroll-configuration';

@Injectable({
  providedIn: 'root'
})
export class PayrollConfigurationService {
  private apiUrl = `${environment.apiUrl}/v1/payroll-configurations`;

  constructor(private http: HttpClient) { }

  getConceptAssignmentsForActiveConfiguration(): Observable<PayrollConfigurationConceptAssignmentListApiResult> {
    return this.http.get<PayrollConfigurationConceptAssignmentListApiResult>(`${this.apiUrl}/concepts`);
  }

  updateConceptAssignmentsForActiveConfiguration(request: UpdateConceptAssignmentsRequest): Observable<PayrollConfigurationConceptAssignmentListApiResult> {
    return this.http.put<PayrollConfigurationConceptAssignmentListApiResult>(`${this.apiUrl}/concepts`, request);
  }

  getActivePayrollConfiguration(): Observable<PayrollConfigurationApiResult> {
    return this.http.get<PayrollConfigurationApiResult>(this.apiUrl);
  }

  createPayrollConfiguration(request: CreatePayrollConfigurationRequest): Observable<PayrollConfigurationApiResult> {
    return this.http.post<PayrollConfigurationApiResult>(this.apiUrl, request);
  }

  deleteActivePayrollConfiguration(): Observable<VoidApiResult> {
    return this.http.delete<VoidApiResult>(this.apiUrl);
  }
}
