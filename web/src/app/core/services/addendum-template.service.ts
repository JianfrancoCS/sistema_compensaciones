import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, signal, inject } from '@angular/core';
import { tap, catchError } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult, SelectOption, StateSelectOptionDTO } from '@shared/types';
import {
  AddendumTemplateListDTO,
  CommandAddendumTemplateResponse,
  CreateAddendumTemplateRequest,
  UpdateAddendumTemplateRequest,
  ApiPagedAddendumTemplatesResponse,
  ApiCommandAddendumTemplateResponse
} from '@shared/types/addendum';

@Injectable({
  providedIn: 'root'
})
export class AddendumTemplateService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/hiring/addendum-templates`;

  private addendumTemplatesState = signal<{ addendumTemplates: AddendumTemplateListDTO[], loading: boolean, totalElements: number, error: string | null }>({ addendumTemplates: [], loading: true, totalElements: 0, error: null });
  public addendumTemplates = this.addendumTemplatesState.asReadonly();

  private currentQueryParams = signal({
    query: '',
    addendumTypePublicId: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  });

  constructor() {
    this.fetchAddendumTemplates();
  }

  private fetchAddendumTemplates(): void {
    this.addendumTemplatesState.update(state => ({ ...state, loading: true, error: null }));

    let params = new HttpParams()
      .set('name', this.currentQueryParams().query)
      .set('page', this.currentQueryParams().page.toString())
      .set('size', this.currentQueryParams().pageSize.toString())
      .set('sortBy', this.currentQueryParams().sortBy)
      .set('sortDirection', this.currentQueryParams().sortDirection);

    if (this.currentQueryParams().addendumTypePublicId) {
      params = params.set('addendumTypePublicId', this.currentQueryParams().addendumTypePublicId);
    }

    this._http.get<ApiPagedAddendumTemplatesResponse>(this._url, { params }).pipe(
      tap(response => {
        if (response.success) {
          this.addendumTemplatesState.set({
            addendumTemplates: response.data.data,
            loading: false,
            totalElements: response.data.totalElements,
            error: null
          });
        } else {
          this.addendumTemplatesState.set({
            addendumTemplates: [],
            loading: false,
            totalElements: 0,
            error: response.message || 'An unknown error occurred.'
          });
        }
      }),
      catchError(err => {
        console.error('Error fetching addendum templates:', err);
        this.addendumTemplatesState.set({
          addendumTemplates: [],
          loading: false,
          totalElements: 0,
          error: err.error?.message || err.message || 'Failed to load addendum templates.'
        });
        return of({ success: false, message: err.error?.message || err.message || 'Failed to load addendum templates.', data: { data: [], totalElements: 0, pageNumber: 0, totalPages: 0, isFirst: true, isLast: true, hasNext: false, hasPrevious: false }, timeStamp: new Date().toISOString() });
      })
    ).subscribe();
  }

  public search(query: string): void {
    this.currentQueryParams.update(params => ({ ...params, query, page: 0 }));
    this.fetchAddendumTemplates();
  }

  public filterByAddendumType(addendumTypePublicId: string): void {
    this.currentQueryParams.update(params => ({ ...params, addendumTypePublicId, page: 0 }));
    this.fetchAddendumTemplates();
  }

  public loadAddendumTemplates(page: number, pageSize: number, sortBy: string, sortDirection: string): void {
    this.currentQueryParams.update(params => ({ ...params, page, pageSize, sortBy: sortBy || 'createdAt', sortDirection }));
    this.fetchAddendumTemplates();
  }

  public create(request: CreateAddendumTemplateRequest): Observable<ApiResult<any>> {
    return this._http.post<ApiResult<any>>(this._url, request).pipe(
      tap(response => {
        if (response.success) {
          this.fetchAddendumTemplates();
        }
      }),
      catchError(err => {
        console.error('Error creating addendum template:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to create addendum template.', data: {}, timeStamp: new Date().toISOString() });
      })
    );
  }

  public update(publicId: string, request: UpdateAddendumTemplateRequest): Observable<ApiResult<any>> {
    return this._http.put<ApiResult<any>>(`${this._url}/${publicId}`, request).pipe(
      tap(response => {
        if (response.success) {
          this.fetchAddendumTemplates();
        }
      }),
      catchError(err => {
        console.error('Error updating addendum template:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to update addendum template.', data: {}, timeStamp: new Date().toISOString() });
      })
    );
  }

  public delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`).pipe(
      tap(response => {
        if (response.success) {
          this.fetchAddendumTemplates();
        }
      }),
      catchError(err => {
        console.error('Error deleting addendum template:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to delete addendum template.', data: undefined, timeStamp: new Date().toISOString() });
      })
    );
  }

  public getAddendumTemplateForCommand(publicId: string): Observable<ApiCommandAddendumTemplateResponse> {
    return this._http.get<ApiCommandAddendumTemplateResponse>(`${this._url}/${publicId}/command`).pipe(
      catchError(err => {
        console.error('Error fetching addendum template for command:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to fetch addendum template for command.', data: {} as CommandAddendumTemplateResponse, timeStamp: new Date().toISOString() });
      })
    );
  }

  public getSelectOptions(addendumTypePublicId?: string): Observable<ApiResult<SelectOption[]>> {
    let params = new HttpParams();
    if (addendumTypePublicId) {
      params = params.set('addendumTypePublicId', addendumTypePublicId);
    }
    return this._http.get<ApiResult<SelectOption[]>>(`${this._url}/select-options`, { params }).pipe(
      catchError(err => {
        console.error('Error fetching addendum template select options:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to fetch addendum template select options.', data: [], timeStamp: new Date().toISOString() });
      })
    );
  }

  public getStatesSelectOptions(): Observable<ApiResult<StateSelectOptionDTO[]>> {
    return this._http.get<ApiResult<StateSelectOptionDTO[]>>(`${this._url}/states/select-options`).pipe(
      catchError(err => {
        console.error('Error fetching addendum template states select options:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to fetch addendum template states select options.', data: [], timeStamp: new Date().toISOString() });
      })
    );
  }
}
