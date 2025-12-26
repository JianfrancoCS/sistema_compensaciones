import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult } from '@shared/types/api';

export interface SubsidiarySignerListDTO {
  subsidiaryPublicId: string;
  subsidiaryName: string;
  responsibleEmployeeDocumentNumber: string | null;
  responsibleEmployeeName: string | null;
  responsiblePosition: string | null;
  signatureImageUrl: string | null;
  hasSigner: boolean;
}

export interface SubsidiarySignerDetailsDTO {
  publicId: string;
  subsidiaryPublicId: string | null;
  subsidiaryName: string;
  responsibleEmployeeDocumentNumber: string;
  responsibleEmployeeName: string;
  responsiblePosition: string;
  signatureImageUrl: string | null;
  notes: string | null;
  createdAt: string;
  createdBy: string;
}

export interface CreateSubsidiarySignerRequest {
  subsidiaryPublicId: string | null; 
  responsibleEmployeeDocumentNumber: string;
  responsiblePosition: string;
  signatureImageUrl?: string | null;
  notes?: string | null;
}

export interface UpdateSubsidiarySignerRequest {
  responsibleEmployeeDocumentNumber?: string;
  responsiblePosition?: string;
  signatureImageUrl?: string | null;
  notes?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class SubsidiarySignerService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/subsidiary-signers`;

  listSubsidiariesWithSigners(): Observable<ApiResult<SubsidiarySignerListDTO[]>> {
    return this._http.get<ApiResult<SubsidiarySignerListDTO[]>>(`${this._url}/all`).pipe(
      map(response => {
        if (response.success && response.data) {
          const data = response.data.map(item => ({
            ...item,
            signatureImageUrl: this.convertToAbsoluteUrl(item.signatureImageUrl)
          }));
          return { ...response, data };
        }
        return response;
      })
    );
  }

  listSubsidiariesWithSignersPaged(params: {
    subsidiaryName?: string;
    responsibleEmployeeName?: string;
    page: number;
    size: number;
    sortBy: string;
    sortDirection: string;
  }): Observable<ApiResult<PagedResult<SubsidiarySignerListDTO>>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection);

    if (params.subsidiaryName) {
      httpParams = httpParams.set('subsidiaryName', params.subsidiaryName);
    }
    if (params.responsibleEmployeeName) {
      httpParams = httpParams.set('responsibleEmployeeName', params.responsibleEmployeeName);
    }

    return this._http.get<ApiResult<PagedResult<SubsidiarySignerListDTO>>>(this._url, { params: httpParams }).pipe(
      map(response => {
        if (response.success && response.data) {
          const data = {
            ...response.data,
            data: response.data.data.map(item => ({
              ...item,
              signatureImageUrl: this.convertToAbsoluteUrl(item.signatureImageUrl)
            }))
          };
          return { ...response, data };
        }
        return response;
      })
    );
  }

  getSignerBySubsidiary(subsidiaryPublicId: string): Observable<ApiResult<SubsidiarySignerDetailsDTO>> {
    return this._http.get<ApiResult<SubsidiarySignerDetailsDTO>>(`${this._url}/subsidiary/${subsidiaryPublicId}`).pipe(
      map(response => {
        if (response.success && response.data) {
          const data = {
            ...response.data,
            signatureImageUrl: this.convertToAbsoluteUrl(response.data.signatureImageUrl)
          };
          return { ...response, data };
        }
        return response;
      })
    );
  }

  createSigner(request: CreateSubsidiarySignerRequest, signatureImage?: File): Observable<ApiResult<SubsidiarySignerDetailsDTO>> {
    const formData = new FormData();
    
    const requestBlob = new Blob([JSON.stringify(request)], { type: 'application/json' });
    formData.append('request', requestBlob);
    
    if (signatureImage) {
      formData.append('signatureImage', signatureImage);
    }
    
    return this._http.post<ApiResult<SubsidiarySignerDetailsDTO>>(this._url, formData).pipe(
      map(response => {
        if (response.success && response.data) {
          const data = {
            ...response.data,
            signatureImageUrl: this.convertToAbsoluteUrl(response.data.signatureImageUrl)
          };
          return { ...response, data };
        }
        return response;
      })
    );
  }

  updateSigner(publicId: string, request: UpdateSubsidiarySignerRequest, signatureImage?: File): Observable<ApiResult<SubsidiarySignerDetailsDTO>> {
    const formData = new FormData();
    
    const requestBlob = new Blob([JSON.stringify(request)], { type: 'application/json' });
    formData.append('request', requestBlob);
    
    if (signatureImage) {
      formData.append('signatureImage', signatureImage);
    }
    
    return this._http.put<ApiResult<SubsidiarySignerDetailsDTO>>(`${this._url}/${publicId}`, formData).pipe(
      map(response => {
        if (response.success && response.data) {
          const data = {
            ...response.data,
            signatureImageUrl: this.convertToAbsoluteUrl(response.data.signatureImageUrl)
          };
          return { ...response, data };
        }
        return response;
      })
    );
  }

  deleteSigner(subsidiaryPublicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/subsidiary/${subsidiaryPublicId}`);
  }

  private convertToAbsoluteUrl(url: string | null | undefined): string | null {
    if (!url) {
      return null;
    }
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    if (url.startsWith('/')) {
      return `${environment.apiUrl}${url}`;
    }
    return `${environment.apiUrl}/${url}`;
  }
}

