import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  Element,
  CreateElementRequest,
  UpdateElementRequest,
  CommandElementResponse,
  ElementDetails,
  ElementParams
} from '@shared/types/element';

@Injectable({
  providedIn: 'root'
})
export class ElementService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/elements`;

  getElements(params: ElementParams): Observable<ApiResult<PagedResult<Element>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('pageSize', params.pageSize.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.query) {
      httpParams = httpParams.set('query', params.query);
    }
    if (params.containerPublicId) {
      httpParams = httpParams.set('containerPublicId', params.containerPublicId);
    }

    return this._http.get<ApiResult<PagedResult<Element>>>(this._url, { params: httpParams });
  }

  create(request: CreateElementRequest): Observable<ApiResult<CommandElementResponse>> {
    return this._http.post<ApiResult<CommandElementResponse>>(this._url, request);
  }

  update(publicId: string, request: UpdateElementRequest): Observable<ApiResult<CommandElementResponse>> {
    return this._http.put<ApiResult<CommandElementResponse>>(`${this._url}/${publicId}`, request);
  }

  delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`);
  }

  getDetails(publicId: string): Observable<ApiResult<ElementDetails>> {
    return this._http.get<ApiResult<ElementDetails>>(`${this._url}/${publicId}`);
  }

  getForUpdate(publicId: string): Observable<ApiResult<CommandElementResponse>> {
    return this._http.get<ApiResult<CommandElementResponse>>(`${this._url}/command/${publicId}`);
  }
}

