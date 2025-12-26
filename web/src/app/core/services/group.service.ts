import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';
import {
  GroupDTO,
  GroupSelectOptionDTO,
  CreateGroupRequest,
  UpdateGroupRolesRequest,
  PermissionAssignmentDTO,
  GroupParams
} from '@shared/types/security';

@Injectable({
  providedIn: 'root'
})
export class GroupService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/groups`;

  getGroups(params: GroupParams): Observable<ApiResult<PagedResult<GroupDTO>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString());

    if (params.search) {
      httpParams = httpParams.set('search', params.search);
    }

    return this._http.get<ApiResult<PagedResult<GroupDTO>>>(this._url, { params: httpParams });
  }

  getSelectOptions(): Observable<ApiResult<GroupSelectOptionDTO[]>> {
    return this._http.get<ApiResult<{ id: string; name: string }[]>>(`${this._url}/select-options`).pipe(
      map((response) => ({
        ...response,
        data: response.data.map(group => ({
          publicId: group.id,
          name: group.name
        }))
      }))
    );
  }

  createGroup(request: CreateGroupRequest): Observable<ApiResult<void>> {
    return this._http.post<ApiResult<void>>(this._url, request);
  }

  syncGroupRoles(groupId: string, request: UpdateGroupRolesRequest): Observable<ApiResult<void>> {
    return this._http.put<ApiResult<void>>(`${this._url}/${groupId}/permissions`, request);
  }

  getGroupPermissionStatus(groupId: string): Observable<ApiResult<PermissionAssignmentDTO[]>> {
    return this._http.get<ApiResult<PermissionAssignmentDTO[]>>(`${this._url}/${groupId}/permissions-status`);
  }

  isGroupValid(groupId: string): Observable<ApiResult<boolean>> {
    return this._http.get<ApiResult<boolean>>(`${this._url}/${groupId}/validate`);
  }

  deleteGroup(groupId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${groupId}`);
  }
}
