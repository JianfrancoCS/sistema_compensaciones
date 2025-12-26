import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import {
  PayrollListApiResult,
  CommandPayrollApiResult,
  PayrollSummaryApiResult,
  VoidApiResult,
  CreatePayrollRequest,
  PayrollPageableRequest,
  ProcessedTareosApiResult,
  PayrollEmployeesApiResult,
  PayrollEmployeeDetailApiResult
} from '@shared/types/payroll';
import { TareoListDTO } from '@shared/types/tareo';

@Injectable({
  providedIn: 'root'
})
export class PayrollService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/v1/payrolls`;

  list(request: PayrollPageableRequest): Observable<PayrollListApiResult> {
    let params = new HttpParams()
      .set('page', request.page.toString())
      .set('size', request.size.toString());

    if (request.sortBy) {
      params = params.set('sortBy', request.sortBy);
    }

    if (request.sortDirection) {
      params = params.set('sortDirection', request.sortDirection);
    }

    if (request.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', request.subsidiaryPublicId);
    }

    if (request.periodPublicId) {
      params = params.set('periodPublicId', request.periodPublicId);
    }

    if (request.status) {
      params = params.set('status', request.status);
    }

    return this.http.get<PayrollListApiResult>(this.API_URL, { params });
  }

  create(request: CreatePayrollRequest): Observable<CommandPayrollApiResult> {
    return this.http.post<CommandPayrollApiResult>(this.API_URL, request);
  }

  launch(publicId: string): Observable<CommandPayrollApiResult> {
    return this.http.post<CommandPayrollApiResult>(`${this.API_URL}/${publicId}/launch`, {});
  }

  delete(publicId: string): Observable<VoidApiResult> {
    return this.http.delete<VoidApiResult>(`${this.API_URL}/${publicId}`);
  }

  getSummary(publicId: string): Observable<PayrollSummaryApiResult> {
    return this.http.get<PayrollSummaryApiResult>(`${this.API_URL}/${publicId}/summary`);
  }

  generatePayslips(publicId: string): Observable<CommandPayrollApiResult> {
    return this.http.post<CommandPayrollApiResult>(`${this.API_URL}/${publicId}/generate-payslips`, {});
  }

  cancel(publicId: string): Observable<CommandPayrollApiResult> {
    return this.http.post<CommandPayrollApiResult>(`${this.API_URL}/${publicId}/cancel`, {});
  }

  getProcessedTareos(publicId: string): Observable<ProcessedTareosApiResult> {
    return this.http.get<ProcessedTareosApiResult>(`${this.API_URL}/${publicId}/processed-tareos`);
  }

  getPayrollEmployees(
    publicId: string,
    laborPublicId?: string | null,
    employeeDocumentNumber?: string | null
  ): Observable<PayrollEmployeesApiResult> {
    let params = new HttpParams();
    if (laborPublicId) {
      params = params.set('laborPublicId', laborPublicId);
    }
    if (employeeDocumentNumber) {
      params = params.set('employeeDocumentNumber', employeeDocumentNumber);
    }
    return this.http.get<PayrollEmployeesApiResult>(`${this.API_URL}/${publicId}/employees`, { params });
  }

  getPayrollEmployeeDetail(
    publicId: string,
    employeeDocumentNumber: string
  ): Observable<PayrollEmployeeDetailApiResult> {
    return this.http.get<PayrollEmployeeDetailApiResult>(
      `${this.API_URL}/${publicId}/employees/${employeeDocumentNumber}`
    );
  }
}
