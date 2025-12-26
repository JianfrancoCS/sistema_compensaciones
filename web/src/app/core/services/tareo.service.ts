import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import {
  TareoListApiResult,
  TareoDailyListApiResult,
  TareoDetailApiResult,
  VoidApiResult,
  TareoPageableRequest,
  TareoDailyPageableRequest
} from '@shared/types/tareo';

@Injectable({
  providedIn: 'root'
})
export class TareoService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/v1/tareos`;

  list(request: TareoPageableRequest): Observable<TareoListApiResult> {
    let params = new HttpParams()
      .set('page', request.page.toString())
      .set('size', request.size.toString());

    if (request.sortBy) {
      params = params.set('sortBy', request.sortBy);
    }

    if (request.sortDirection) {
      params = params.set('sortDirection', request.sortDirection);
    }

    if (request.laborPublicId) {
      params = params.set('laborPublicId', request.laborPublicId);
    }

    if (request.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', request.subsidiaryPublicId);
    }

    if (request.createdBy) {
      params = params.set('createdBy', request.createdBy);
    }

    if (request.dateFrom) {
      params = params.set('dateFrom', request.dateFrom);
    }

    if (request.dateTo) {
      params = params.set('dateTo', request.dateTo);
    }

    if (request.isProcessed !== undefined && request.isProcessed !== null) {
      params = params.set('isProcessed', request.isProcessed.toString());
    }

    return this.http.get<TareoListApiResult>(this.API_URL, { params });
  }

  delete(publicId: string): Observable<VoidApiResult> {
    return this.http.delete<VoidApiResult>(`${this.API_URL}/${publicId}`);
  }

  listDaily(request: TareoDailyPageableRequest): Observable<TareoDailyListApiResult> {
    let params = new HttpParams()
      .set('page', request.page.toString())
      .set('size', request.size.toString());

    if (request.sortBy) {
      params = params.set('sortBy', request.sortBy);
    }

    if (request.sortDirection) {
      params = params.set('sortDirection', request.sortDirection);
    }

    if (request.laborPublicId) {
      params = params.set('laborPublicId', request.laborPublicId);
    }

    if (request.subsidiaryPublicId) {
      params = params.set('subsidiaryPublicId', request.subsidiaryPublicId);
    }

    if (request.dateFrom) {
      params = params.set('dateFrom', request.dateFrom);
    }

    if (request.dateTo) {
      params = params.set('dateTo', request.dateTo);
    }

    if (request.isCalculated !== undefined && request.isCalculated !== null) {
      params = params.set('isCalculated', request.isCalculated.toString());
    }

    return this.http.get<TareoDailyListApiResult>(`${this.API_URL}/daily`, { params });
  }

  getDetail(publicId: string): Observable<TareoDetailApiResult> {
    return this.http.get<TareoDetailApiResult>(`${this.API_URL}/${publicId}`);
  }
}

