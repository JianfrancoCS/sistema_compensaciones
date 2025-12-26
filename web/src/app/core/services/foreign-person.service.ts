import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import {
  ApiCreateForeignPersonResponse,
  ApiUpdateForeignPersonResponse,
  CreateForeignPersonRequest,
  UpdateForeignPersonRequest,
  ApiResult
} from '@shared/types';

@Injectable({
  providedIn: 'root'
})
export class ForeignPersonService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/persons`;

  createForeignPerson(request: CreateForeignPersonRequest): Observable<ApiCreateForeignPersonResponse> {
    return this._http.post<ApiCreateForeignPersonResponse>(`${this._url}/foreign`, request);
  }

  updateForeignPerson(documentNumber: string, request: UpdateForeignPersonRequest): Observable<ApiUpdateForeignPersonResponse> {
    return this._http.put<ApiUpdateForeignPersonResponse>(`${this._url}/foreign/${documentNumber}`, request);
  }

  findPersonByDocument(documentNumber: string, isNational: boolean = false): Observable<ApiResult<any>> {
    const params = new HttpParams().set('isNational', isNational.toString());
    return this._http.get<ApiResult<any>>(`${this._url}/${documentNumber}`, { params });
  }
}
