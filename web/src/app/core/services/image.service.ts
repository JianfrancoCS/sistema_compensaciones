import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap, of } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult } from '@shared/types/api';

export interface SignatureUrlResponse {
  uploadUrl: string;
  apiKey: string;
  timestamp: number;
  signature: string;
  folder: string;
}

export enum Bucket {
  EMPLOYEE_PROFILE = 'EMPLOYEE_PROFILE',
  COMPANY_LOGO = 'COMPANY_LOGO',
  CONTRACT_DOCUMENT = 'CONTRACT_DOCUMENT',
  MENU_ICON = 'MENU_ICON',
  SIGNATURE = 'SIGNATURE'
}

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/images`;

  getUploadSignature(bucket: Bucket): Observable<ApiResult<SignatureUrlResponse>> {
    const params = new HttpParams().set('bucket', bucket);
    return this._http.get<ApiResult<SignatureUrlResponse>>(`${this._url}/upload/signature`, { params });
  }

  uploadToCloudinary(file: File, bucket: Bucket): Observable<string> {
    return this.getUploadSignature(bucket).pipe(
      switchMap((response) => {
        if (!response.success || !response.data) {
          throw new Error(response.message || 'Error al obtener URL de subida');
        }

        const { uploadUrl, apiKey, timestamp, signature, folder } = response.data;
        const formData = new FormData();
        formData.append('file', file);
        formData.append('api_key', apiKey);
        formData.append('timestamp', timestamp.toString());
        formData.append('signature', signature);
        formData.append('folder', folder);

        return this._http.post<any>(uploadUrl, formData).pipe(
          switchMap((uploadRes: any) => {
            if (uploadRes.secure_url) {
              return of(uploadRes.secure_url);
            } else {
              throw new Error('No se recibió URL de la imagen subida');
            }
          })
        );
      })
    );
  }
}

