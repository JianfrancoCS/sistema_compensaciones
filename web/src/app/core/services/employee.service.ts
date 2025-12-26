import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult, StateSelectOptionDTO } from '@shared/types';
import {
  EmployeeListDTO,
  EmployeeDetailsDTO,
  CreateEmployeeRequest,
  UpdateEmployeeRequest,
  CommandEmployeeResponse,
  EmployeeParams
} from '@shared/types/employee';

export type {
  UpdateEmployeeRequest,
  CreateEmployeeRequest,
  CommandEmployeeResponse,
  EmployeeDetailsDTO,
  EmployeeListDTO
};

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/employees`;

  public getEmployees(params: EmployeeParams): Observable<ApiResult<PagedResult<EmployeeListDTO>>> {
    let httpParams = new HttpParams();
    if (params.documentNumber) {
      httpParams = httpParams.set('documentNumber', params.documentNumber);
    }
    if (params.personName) {
      httpParams = httpParams.set('personName', params.personName);
    }
    if (params.subsidiaryPublicId) {
      httpParams = httpParams.set('subsidiaryPublicId', params.subsidiaryPublicId);
    }
    if (params.positionPublicId) {
      httpParams = httpParams.set('positionPublicId', params.positionPublicId);
    }
    if (params.isNational !== undefined) {
      httpParams = httpParams.set('isNational', params.isNational.toString());
    }
    httpParams = httpParams.set('page', params.page.toString());
    httpParams = httpParams.set('size', params.size.toString());
    httpParams = httpParams.set('sortBy', params.sortBy);
    httpParams = httpParams.set('sortDirection', params.sortDirection);

    return this._http.get<ApiResult<PagedResult<EmployeeListDTO>>>(this._url, { params: httpParams });
  }

  public getEmployeeDetails(code: string): Observable<ApiResult<EmployeeDetailsDTO>> {
    return this._http.get<ApiResult<EmployeeDetailsDTO>>(`${this._url}/${code}`);
  }

  public getEmployeeForEdit(publicId: string): Observable<ApiResult<CommandEmployeeResponse>> {
    return this._http.get<ApiResult<CommandEmployeeResponse>>(`${this._url}/command/${publicId}`);
  }

  public create(request: CreateEmployeeRequest): Observable<ApiResult<CommandEmployeeResponse>> {
    return this._http.post<ApiResult<CommandEmployeeResponse>>(this._url, request);
  }

  public update(code: string, request: UpdateEmployeeRequest): Observable<ApiResult<any>> {
    return this._http.patch<ApiResult<any>>(`${this._url}/${code}`, request);
  }

  public delete(publicId: string): Observable<ApiResult<any>> {
    return this._http.delete<ApiResult<any>>(`${this._url}/${publicId}`);
  }

  public getStatesForSelect(): Observable<ApiResult<StateSelectOptionDTO[]>> {
    return this._http.get<ApiResult<StateSelectOptionDTO[]>>(`${this._url}/states/select-options`);
  }

  public searchByDocumentNumber(documentNumber: string): Observable<ApiResult<{ documentNumber: string; fullName: string; position: string; subsidiaryId: string; subsidiaryName: string }>> {
    const params = new HttpParams().set('documentNumber', documentNumber);
    return this._http.get<ApiResult<{ documentNumber: string; fullName: string; position: string; subsidiaryId: string; subsidiaryName: string }>>(`${this._url}/search`, { params });
  }

  public uploadPhoto(documentNumber: string, file: File): Observable<ApiResult<string>> {
    const formData = new FormData();
    formData.append('file', file);
    return this._http.post<ApiResult<string>>(`${this._url}/${documentNumber}/photo`, formData);
  }

  public getPhoto(documentNumber: string): Observable<ApiResult<string | null>> {
    return this._http.get<ApiResult<string | null>>(`${this._url}/${documentNumber}/photo`);
  }
}
