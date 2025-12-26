import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import {
  ConceptListDTO,
  ConceptDetailsDTO,
  CreateConceptRequest,
  UpdateConceptRequest,
  ConceptParams,
  ConceptSelectOptionDTO,
  ConceptListApiResult,
  ConceptDetailsApiResult,
  CommandConceptApiResult,
  ConceptSelectOptionListApiResult
} from '@shared/types/concept';
import { ApiResult } from '@shared/types/api';

@Injectable({
  providedIn: 'root'
})
export class ConceptService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/concepts`;

  getConcepts(params: ConceptParams): Observable<ConceptListApiResult> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }

    if (params.categoryPublicId) {
      httpParams = httpParams.set('categoryPublicId', params.categoryPublicId);
    }

    return this._http.get<ConceptListApiResult>(this._url, { params: httpParams });
  }

  getSelectOptions(): Observable<ConceptSelectOptionListApiResult> {
    return this._http.get<ConceptSelectOptionListApiResult>(`${this._url}/select-options`);
  }

  getSelectOptionsByCategory(categoryCode: string): Observable<ConceptSelectOptionListApiResult> {
    return this._http.get<ConceptSelectOptionListApiResult>(`${this._url}/select-options/by-category/${categoryCode}`);
  }

  create(request: CreateConceptRequest): Observable<CommandConceptApiResult> {
    return this._http.post<CommandConceptApiResult>(this._url, request);
  }

  update(publicId: string, request: UpdateConceptRequest): Observable<CommandConceptApiResult> {
    return this._http.put<CommandConceptApiResult>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }

  getDetails(publicId: string): Observable<ConceptDetailsApiResult> {
    return this._http.get<ConceptDetailsApiResult>(`${this._url}/${publicId}`);
  }

  getCategories(): Observable<ApiResult<Array<{publicId: string, code: string, name: string}>>> {
    return this._http.get<ApiResult<Array<{publicId: string, code: string, name: string}>>>(`${this._url}/categories`);
  }
}
