import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult, SelectOption } from '@shared/types/api';
import {
  Position,
  CreatePositionRequest,
  UpdatePositionRequest,
  PositionDetailsForUpdateDTO,
  PositionParams
} from '@shared/types/position';

export type {
  CreatePositionRequest,
  UpdatePositionRequest,
  PositionDetailsForUpdateDTO,
  Position
};

@Injectable({
  providedIn: 'root'
})
export class PositionService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/positions`;

  public getPositions(params: PositionParams): Observable<ApiResult<PagedResult<Position>>> {
    let httpParams = new HttpParams();
    if (params.name) {
      httpParams = httpParams.set('name', params.name);
    }
    if (params.areaPublicId) {
      httpParams = httpParams.set('areaPublicId', params.areaPublicId);
    }
    httpParams = httpParams.set('page', params.page.toString());
    httpParams = httpParams.set('size', params.size.toString());
    httpParams = httpParams.set('sortBy', params.sortBy);
    httpParams = httpParams.set('sortDirection', params.sortDirection);

    return this._http.get<ApiResult<PagedResult<Position>>>(this._url, { params: httpParams });
  }

  public create(request: CreatePositionRequest): Observable<ApiResult<Position>> {
    return this._http.post<ApiResult<Position>>(this._url, request);
  }

  public update(publicId: string, request: UpdatePositionRequest): Observable<ApiResult<Position>> {
    return this._http.put<ApiResult<Position>>(`${this._url}/${publicId}`, request);
  }

  public delete(publicId: string): Observable<ApiResult<any>> {
    return this._http.delete<ApiResult<any>>(`${this._url}/${publicId}`);
  }

  public getPositionDetailsForUpdate(publicId: string): Observable<ApiResult<PositionDetailsForUpdateDTO>> {
    return this._http.get<ApiResult<PositionDetailsForUpdateDTO>>(`${this._url}/command/${publicId}`);
  }

  public getPositionsSelectOptions(areaPublicId?: string): Observable<ApiResult<SelectOption[]>> {
    let params = new HttpParams();
    if (areaPublicId) {
      params = params.set('areaPublicId', areaPublicId);
    }
    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/select-options`, { params });
  }

  public getSelectOptions(areaPublicId?: string): Observable<ApiResult<SelectOption[]>> {
    return this.getPositionsSelectOptions(areaPublicId);
  }
}
