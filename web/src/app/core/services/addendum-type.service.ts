import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, SelectOption } from '@shared/types/api';
import {
  CreateAddendumTypeRequest,
  UpdateAddendumTypeRequest,
  CommandAddendumTypeResponse
} from '@shared/types/addendum';

@Injectable({
  providedIn: 'root'
})
export class AddendumTypeService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/hiring/addendum-types`;

  getAddendumTypeSelectOptions(): Observable<ApiResult<SelectOption[]>> {
    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/select-options`).pipe(
      catchError(err => {
        console.error('Error fetching addendum type select options:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to fetch addendum type select options.', data: [], timeStamp: new Date().toISOString() });
      })
    );
  }

  create(request: CreateAddendumTypeRequest): Observable<ApiResult<CommandAddendumTypeResponse>> {
    return this._http.post<ApiResult<CommandAddendumTypeResponse>>(this._url, request).pipe(
      catchError(err => {
        console.error('Error creating addendum type:', err);
        const message = err.error?.message || err.message || 'Failed to create addendum type.';
        return of({ success: false, message, data: null as any, timeStamp: new Date().toISOString() });
      })
    );
  }

  update(publicId: string, request: UpdateAddendumTypeRequest): Observable<ApiResult<CommandAddendumTypeResponse>> {
    return this._http.put<ApiResult<CommandAddendumTypeResponse>>(`${this._url}/${publicId}`, request).pipe(
      catchError(err => {
        console.error(`Error updating addendum type ${publicId}:`, err);
        const message = err.error?.message || err.message || 'Failed to update addendum type.';
        return of({ success: false, message, data: null as any, timeStamp: new Date().toISOString() });
      })
    );
  }

  getAddendumTypeForCommand(publicId: string): Observable<ApiResult<CommandAddendumTypeResponse>> {
    return this._http.get<ApiResult<CommandAddendumTypeResponse>>(`${this._url}/command/${publicId}`).pipe(
      catchError(err => {
        console.error(`Error fetching addendum type ${publicId}:`, err);
        const message = err.error?.message || err.message || 'Failed to fetch addendum type.';
        return of({ success: false, message, data: null as any, timeStamp: new Date().toISOString() });
      })
    );
  }
}
