import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiDocumentTypesResponse } from '@shared/types';

@Injectable({
  providedIn: 'root'
})
export class DocumentTypeService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/persons/document-types`;

  getDocumentTypeSelectOptions(): Observable<ApiDocumentTypesResponse> {
    return this._http.get<ApiDocumentTypesResponse>(`${this._url}/select-options`);
  }
}
