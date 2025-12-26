import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult } from '@shared/types/api';

export interface InternalFileDTO {
  publicId: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  category?: string;
  description?: string;
  createdAt?: string;
  downloadUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class InternalFileService {
  private readonly _http = inject(HttpClient);
  private readonly _apiUrl = environment.apiUrl;
  private readonly _baseUrl = `${this._apiUrl}/v1/internal-files`;

  getAuthenticatedImageUrl(url: string | null | undefined): Observable<string | null> {
    if (!url) {
      return new Observable(observer => {
        observer.next(null);
        observer.complete();
      });
    }

    const absoluteUrl = this.convertToAbsoluteUrl(url);

    return this._http.get(absoluteUrl, {
      responseType: 'blob',
      headers: new HttpHeaders({
        'Accept': 'image/*'
      })
    }).pipe(
      map(blob => {
        const blobUrl = URL.createObjectURL(blob);
        return blobUrl;
      })
    );
  }

  private convertToAbsoluteUrl(url: string): string {
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    if (url.startsWith('/')) {
      return `${this._apiUrl}${url}`;
    }
    return `${this._apiUrl}/${url}`;
  }

  revokeBlobUrl(blobUrl: string | null): void {
    if (blobUrl && blobUrl.startsWith('blob:')) {
      URL.revokeObjectURL(blobUrl);
    }
  }

  uploadFile(
    file: File,
    fileableId: number,
    fileableType: string,
    category?: string,
    description?: string,
    activePublicIds?: string[] | null
  ): Observable<ApiResult<InternalFileDTO>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileableId', fileableId.toString());
    formData.append('fileableType', fileableType);
    
    if (category) {
      formData.append('category', category);
    }
    if (description) {
      formData.append('description', description);
    }

    let params = new HttpParams();
    if (activePublicIds !== undefined && activePublicIds !== null) {
      activePublicIds.forEach(id => {
        params = params.append('activePublicIds', id);
      });
    }

    return this._http.post<ApiResult<InternalFileDTO>>(
      `${this._baseUrl}/upload`,
      formData,
      { params }
    );
  }

  uploadMultipleFiles(
    files: File[],
    fileableId: number,
    fileableType: string,
    category?: string,
    description?: string,
    activePublicIds?: string[] | null
  ): Observable<ApiResult<InternalFileDTO[]>> {
    const formData = new FormData();
    files.forEach(file => {
      formData.append('files', file);
    });
    formData.append('fileableId', fileableId.toString());
    formData.append('fileableType', fileableType);
    
    if (category) {
      formData.append('category', category);
    }
    if (description) {
      formData.append('description', description);
    }

    let params = new HttpParams();
    if (activePublicIds !== undefined && activePublicIds !== null) {
      activePublicIds.forEach(id => {
        params = params.append('activePublicIds', id);
      });
    }

    return this._http.post<ApiResult<InternalFileDTO[]>>(
      `${this._baseUrl}/upload-multiple`,
      formData,
      { params }
    );
  }

  synchronizeActiveFiles(
    fileableId: number,
    fileableType: string,
    activePublicIds: string[] | null
  ): Observable<ApiResult<void>> {
    let params = new HttpParams()
      .set('fileableId', fileableId.toString())
      .set('fileableType', fileableType);

    if (activePublicIds !== null) {
      activePublicIds.forEach(id => {
        params = params.append('activePublicIds', id);
      });
    }

    return this._http.post<ApiResult<void>>(
      `${this._baseUrl}/synchronize`,
      null,
      { params }
    );
  }

  deleteFile(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._baseUrl}/${publicId}`);
  }

  deleteFiles(publicIds: string[]): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._baseUrl}/batch`, {
      body: publicIds
    });
  }

  getFilesByFileable(
    fileableId: number,
    fileableType: string,
    category?: string
  ): Observable<ApiResult<InternalFileDTO[]>> {
    let params = new HttpParams()
      .set('fileableId', fileableId.toString())
      .set('fileableType', fileableType);
    
    if (category) {
      params = params.set('category', category);
    }

    return this._http.get<ApiResult<InternalFileDTO[]>>(
      `${this._baseUrl}/fileable/${fileableType}/${fileableId}`,
      { params: category ? params : undefined }
    );
  }
}
