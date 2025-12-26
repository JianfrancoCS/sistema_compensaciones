import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, SelectOption } from '@shared/types/api';
import { DistrictDetailResponseDTO } from '@shared/types/location';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/locations`;

  getDepartments(): Observable<ApiResult<SelectOption[]>> {
    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/departments`);
  }

  getProvincesByDepartmentId(departmentId: string): Observable<ApiResult<SelectOption[]>> {
    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/departments/${departmentId}/provinces`);
  }

  getDistrictsByProvinceId(provinceId: string): Observable<ApiResult<SelectOption[]>> {
    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/provinces/${provinceId}/districts`);
  }

  getDistrictDetails(districtId: string): Observable<ApiResult<DistrictDetailResponseDTO>> {
    return this._http.get<ApiResult<DistrictDetailResponseDTO>>(`${this._url}/districts/${districtId}`);
  }
}
