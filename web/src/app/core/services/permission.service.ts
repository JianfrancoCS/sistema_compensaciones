import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult } from '@shared/types/api';
import { RoleDTO } from '@shared/types/security';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/permissions`;

  getAvailableRoles(): Observable<ApiResult<RoleDTO[]>> {
    return this._http.get<ApiResult<RoleDTO[]>>(this._url);
  }
}
